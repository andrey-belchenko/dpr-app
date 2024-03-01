package cc.datafabric.adapter.lib.exchange.flows

import cc.datafabric.adapter.lib.exchange.common.BsonUtils
import org.bson.Document

abstract class ExchangeOperation  {
    abstract fun getTriggerName():String?;
    var src: String? = null
    var idSource: String? = null
}
