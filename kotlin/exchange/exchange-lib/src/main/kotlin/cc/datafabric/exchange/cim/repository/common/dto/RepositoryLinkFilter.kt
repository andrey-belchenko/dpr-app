package cc.datafabric.exchange.cim.repository.common.dto

class RepositoryLinkFilter(
    var fromId: Iterable<String>?=null,
    var toId: Iterable<String>?=null,
    var fromType: Iterable<String>?=null,
    var toType: Iterable<String>?=null,
    var predicate: Iterable<String>?=null,
)