import org.junit.Assert._
import org.xmlparse.XMLValidator
import scala.io.Source
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.WildcardFileFilter


object ActivityXMLTest {

  def main(args: Array[String]): Unit = {

    val xmlPath_test = "input_xml/activity.xml"
    val xsdPath_path = "Schema_Test_xml/activity_schema.xsd"
    val xmlValidator = new XMLValidator

    assert(xmlPath_test == 1)

  }

  def test2(): Unit = {

    val xmlPath_test = "input_xml/activity.xml"
    val xsdPath_path = "Schema_Test_xml/activity_schema.xsd"
    val xmlValidator = new XMLValidator

    assert(xsdPath_path == 1)
  }
}