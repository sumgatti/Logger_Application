package org.xmlparse.scala

import java.nio.file.Paths

import javax.xml.XMLConstants
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, explode}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{ArrayType, StringType, StructType}
import org.apache.spark.sql.{SparkSession, types}
import javax.xml.XMLConstants
import com.databricks.spark.xml._
import javax.xml.transform.stream.StreamSource
import org.xmlparse
import java.io.File
import scala.io.Source
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.WildcardFileFilter
import org.xmlparse.XMLValidator

object ActivityXML {
  def main(args: Array[String]): Unit = {
    val FileExtensions = List("xml", "csv", "json")

    //Entry point to underlying Spark functionality to programmatically create Spark DataFrame and DataSet
    val spark = SparkSession.builder().master("local[*]")
      .appName("Activity")
      .getOrCreate()

    //Spark log configuration
    spark.sparkContext.setLogLevel("ERROR")

    //Hadoop home directory in windows
    System.setProperty("hadoop.home.dir", "C:\\winutils")

    //Set the path to validate xml files with schema
    val xmlPath = "input_xml/activity.xml"
    val xsdPath = "Schema_xml/activity_schema.xsd"
    val xmlValidator = new XMLValidator
    var final_df_xml = spark.emptyDataFrame
    var final_df_json = spark.emptyDataFrame
    var final_df_csv = spark.emptyDataFrame

    //Validate the main XML file and if validation is successful then proceed
    if (xmlValidator.validate(xmlPath, xsdPath)) {

      //if file extension is csv, parse the files to store into a dataframe
      for (name <- FileExtensions if name.startsWith("csv")) {
        final_df_csv = spark.read.format("csv")
          .option("header", "true")
          .option("delimiter", ",")
          .load("input_csv/activitycode_validation.csv")
      }

      //if file extension is xml, parse the files to store into a dataframe
      for (name <- FileExtensions if name.startsWith("xml")) {
        val schema_xml = new StructType()
          .add("userName", StringType)
          .add("websiteName", StringType)
          .add("activityTypeCode", StringType)
          .add("loggedInTime", StringType)
          .add("number_of_views", StringType)

        val df_xml = spark.read
          .format("xml")
          .option("rowTag", "activity")
          .schema(schema_xml)
          .load("input_xml/*.xml")

        //Join CSV dataframe with XML on activity_code to fetch activity_description
        val featureDf  = df_xml.as("df1").join(final_df_csv.as("df2"), df_xml("activityTypeCode") === final_df_csv("activity_code")).select("df1.userName" , "df1.websiteName","df2.acitivty_description",
        "df1.loggedInTime")

        //Change the field names in the final dataframe and cast the date format to Timestamp
        final_df_xml = featureDf.selectExpr("userName as  user",
                                                    "websiteName as website",
                                                    "acitivty_description as activityTypeDescription",
                                                    "CAST(UNIX_TIMESTAMP(loggedInTime, 'yyyy-MM-dd') AS TIMESTAMP) as signedInTime")
        final_df_xml = featureDf

      }

      //if file extension is json, parse the files to store into a dataframe
      for (name <- FileExtensions if name.startsWith("json")) {
        val df_json = spark.read.option("multiline", true).json("input_json/activity.json")
        var column_count = 0

        //Create a view from dataframe and extract the fields
        df_json.createOrReplaceTempView("activity")
        val teenagerNamesDF = spark.sql("SELECT activity.userName,activity.websiteName ,activity.activityTypeDescription , activity.signedInTime FROM activity ")

        //Cast the date format to Timestamp and create a final dataframe
        final_df_json= teenagerNamesDF.selectExpr("userName as  user",
          "websiteName as website",
          "activityTypeDescription as activityTypeDescription",
          "CAST(UNIX_TIMESTAMP(signedInTime, 'MM/dd/yyyy') AS TIMESTAMP) as signedInTime")
        final_df_json.show()
      }

    }
    else
    {
        //if xml file or schema are not valid
        println("Xml file or schema is not valid, check the file")
    }

        //Writing into a Json File by merging the dataframes from XML & JSON inputs
        val final_aggregate_df = final_df_xml.union(final_df_json)

        /*Writing output Json file by Overwriting every time, this can be changed to append if the input files
        are different */
        final_aggregate_df.write.format("json").mode("overwrite").save("output/jsonRecords/")
}

}
