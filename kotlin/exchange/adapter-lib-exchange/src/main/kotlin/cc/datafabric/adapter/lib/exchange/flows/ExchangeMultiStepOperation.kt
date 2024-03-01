package cc.datafabric.adapter.lib.exchange.flows

import org.bson.Document

open class ExchangeMultiStepOperation() : ExchangeOperation(){
    val operation: MutableList<ExchangeOperation> = mutableListOf()
    var enabled: Boolean = true
    var isParallel:Boolean = false

    override fun getTriggerName():String?{
        return operation[0].getTriggerName()
    }

    protected fun initOperationProps( document: Document){
        this.enabled = document["enabled"] as Boolean? ?: true
        this.isParallel = document["isParallel"] as Boolean? ?: false

        val idSource = document["idSource"]?.toString()
        val docOper = this.getOperations(document)

        val operations = docOper.map {
            val oper: ExchangeOperation = if (it["operation"]==null){
                ExchangeFlow.fromDocument(it)
            }else{
                fromDocument(it)
            }
            if (oper.src==null){
                oper.src =  document["src"]?.toString()
            }
            oper.src = oper.src?.split("\\src\\","/src/")?.last()?.replace("\\","/")
            if (oper.idSource==null){
                oper.idSource =idSource
            }
            oper
        }
        this.operation.addAll(operations)
    }



    open fun getOperations(document: Document):Iterable<Document>{
//        if (document["operation"]==null){
//            val x =0
//        }

        return (document["operation"] as Iterable<Document>)
    }

    companion object {
        fun fromDocument(document: Document): ExchangeMultiStepOperation {
            val obj = ExchangeMultiStepOperation()
            obj.initOperationProps(document)
            return obj
        }




    }
}
