import java.util.*
import javax.xml.stream.events.XMLEvent

interface IXmlProcessor {
    fun processElement(processorOpenElements: Stack<XmlReader.ProcessorOpenElement>)
    fun processText(processorOpenElements: Stack<XmlReader.ProcessorOpenElement>, xmlEvent: XMLEvent)
}