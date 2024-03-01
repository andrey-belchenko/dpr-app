
import java.util.*
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.StartElement
import javax.xml.stream.events.XMLEvent

class  XmlReader (private val reader:XMLEventReader) {

    // todo проанализировать, сделать рефакторинг
    class ProcessorOpenElement (val startElement:StartElement){
        var customData:Any? = null
    }


    private  val openElements = Stack<StartElement>()
    private  val processorOpenElements = Stack<ProcessorOpenElement>()
    private  var lastProcessorElement:ProcessorOpenElement? = null

    fun goToDescendant(tags:Iterable<String>?): XmlReader{
        return goToDescendant(tags,false)
    }

    fun goToChild(tag:String): XmlReader{
        return goToChild(listOf(tag))
    }

    fun goToChild(tags:Iterable<String>?): XmlReader{
        return goToDescendant(tags,true)
    }

    fun goToChild(): XmlReader{
        return goToDescendant(null,true)
    }

    private fun goToDescendant(tags:Iterable<String>?, isImmediate: Boolean): XmlReader {
        val initialDepth = openElements.count()
        while (!isEnd) {
            goToNext(initialDepth+1)
            if (startElement==null){
                break
            }
            if (tags==null || tags.contains(getCurrentTag())) {
                if (!isImmediate || openElements.count() == initialDepth + 1) {
                    break
                }
            }
        }
        return this
    }

//        fun following(): XmlReader {
//            val initialDepth = openElements.count()
//            while (!isEnd) {
//                next(initialDepth)
//                if (openElements.count() <= initialDepth) {
//                    break
//                }
//            }
//            return this
//        }

    fun goToFollowing(): XmlReader {
        var initialDepth = openElements.count()
        if (startElement == null) {
            initialDepth += 1
        }
        while (!isEnd) {
            goToNext(initialDepth)
            if (openElements.count() <= initialDepth) {
                break
            }
        }
        return this
    }

    //        private var currentEvent: XMLEvent? = null
    private var startElement: StartElement? = null
    //        private var lastStartElement: StartElement? = null
    var isEnd:Boolean = false


    private fun readCurrent(){
        if (startElement==null){
            throw  Exception()
        }
        processElement()
        val initialDepth = openElements.count()
        while (true) {
            val currentEvent = reader.nextEvent()
            if (currentEvent!!.isStartElement) {
                startElement = currentEvent.asStartElement()
                openElements.push(startElement)
                processElement()
            } else if (currentEvent.isEndElement) {
                if (initialDepth==openElements.count()){
                    val x = 6
                }
                endProcessElement()
                startElement = null
                if (initialDepth==openElements.count()){
                    openElements.pop()
                    break
                }
                openElements.pop()
            } else if (currentEvent.isCharacters){

                processText(currentEvent)
            }
        }

        val x =1

    }

    private fun goToNext(minimalDepth:Int): XmlReader {
        while (reader.hasNext()) {
            val currentEvent = reader.nextEvent()
            if (currentEvent.isStartElement) {
                startElement = currentEvent.asStartElement()
                openElements.push(startElement)
                break
            } else if (currentEvent.isEndElement) {
                startElement = null
                if (minimalDepth==openElements.count()+1){
                    openElements.pop()
                    break
                }
                openElements.pop()
            }
        }
        isEnd = !reader.hasNext()
        return this
    }




    private fun processElement() {
        if (processor == null) return
//            if (lastStartElement==null){
//                val x = 1
//            }
        processorOpenElements.push(ProcessorOpenElement(startElement!!))
        processor?.processElement(processorOpenElements)
    }

    private fun processText(xmlEvent: XMLEvent) {
        if (processor == null) return
        processor?.processText(processorOpenElements,xmlEvent)
    }

    private fun endProcessElement() {
        if (processor == null) return

        if (processorOpenElements.empty()){
            lastProcessorElement = null
            return
        }
        lastProcessorElement =  processorOpenElements.pop()
    }

    private fun getCurrentElement():StartElement?{
        return startElement
    }

    fun getCurrentTag():String? {
        return getCurrentElement()?.name?.localPart
    }


    fun hasCurrent():Boolean {
        return startElement != null
    }


    private fun setProcessor(processor: IXmlProcessor){
        clearProcessor()
        this.processor = processor
//        processElement()


    }

    private fun clearProcessor(){
        this.processor = null
        processorOpenElements.clear()
    }
    fun readElement(processor: IXmlProcessor):Any? {
        setProcessor(processor)
        this.processor = processor
        readCurrent()
        val value = this.lastProcessorElement?.customData
        clearProcessor()
        return value
    }
    private var processor:IXmlProcessor? = null
}

