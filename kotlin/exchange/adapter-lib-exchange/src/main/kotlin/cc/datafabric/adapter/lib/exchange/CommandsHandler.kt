// todo ПОКА ОТКАЗАЛСЯ ОТ ЭТОГО. УБРАТЬ ЕСЛИ НЕ ПОНАДОБИТСЯ
//package cc.datafabric.adapter.lib.exchange
//
//import cc.datafabric.adapter.lib.exchange.flows.ExchangeFlow
//import org.bson.Document
//
//object CommandsHandler {
//    // todo Реализовано получение команд через БД. Заменить на API?
//    fun handle(commands:Iterable<Document>) {
//        commands.forEach { cmd ->
//            handle(cmd)
//        }
//    }
//
//    private fun handle(command:Document) {
//        val operations = command[Names.Fields.operations] as List<Document>
//        operations.forEach { op ->
//            when (val cmdName = op[Names.Fields.name].toString()) {
//                "setFlowsStateByTags" -> setFlowsStateByTags(op)
//                "runFlowsByTags" -> runFlowsByTags(op)
//                "runFlowByName" -> runFlowByName(op)
//                else -> {
//                    throw NotImplementedError("Handler for command $cmdName is not implemented")
//                }
//            }
//        }
//        //todo, переделать управление этими параметрами, может быть сделать параметром функции put
//        ExchangeStore.useTimer = false
//        ExchangeStore.doMessageLogging = false
//        ExchangeStore.put(Names.Collections.processorCompletedCommand,command)
//        ExchangeStore.applyInsert()
//    }
//
//    private fun getStrArray(doc:Document,fieldName:String):Iterable<String>{
//      return  (doc[fieldName] as List<*>).map { it.toString() }
//    }
//
//    private fun getString(doc:Document,fieldName:String):String?{
//        return  doc[fieldName]?.toString()
//    }
//
//    private fun getBool(doc:Document,fieldName:String):Boolean{
//        if (doc[fieldName]==null){
//            return false
//        }
//        return  doc[fieldName] as Boolean
//    }
//
////    private fun getFilterType(doc:Document):ExchangeFlow.FilterType{
////        var filterTypeStr = getString(doc,"filterType")
////        if (filterTypeStr==null){
////            filterTypeStr =  ExchangeFlow.FilterType.default.toString()
////        }
////        return  ExchangeFlow.FilterType.valueOf(filterTypeStr)
////    }
//
//    private fun setFlowsStateByTags(command:Document) {
//        ExchangeStatusStore.setFlowStatesTags(
//            getStrArray(command,"disabled").toSet(),
//            getStrArray(command,"enabled").toSet()
//        )
//    }
//    private fun runFlowsByTags(command:Document) {
//        val tags = getStrArray(command,"tags").toSet()
//        val rules = ExchangeSettingsRepository.getByTags(tags)
//        ExchangeProcessor.runRules(rules,getBool(command,"onlyChanges"))
//    }
//
//    private fun runFlowByName(command:Document) {
//        val name = getString(command,"name")!!
//        val rule = ExchangeSettingsRepository.getByName(name)!!
//        ExchangeProcessor.runRules(listOf(rule),getBool(command,"onlyChanges"))
//    }
//}