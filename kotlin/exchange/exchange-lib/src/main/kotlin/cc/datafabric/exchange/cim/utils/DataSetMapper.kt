package cc.datafabric.exchange.cim.utils

import cc.datafabric.exchange.cim.model.DataSet
import cc.datafabric.linesapp.sys.model.ModelObject
import cc.datafabric.linesapp.sys.model.ReflectionUtils
import cc.datafabric.exchange.cim.repository.common.dto.RepositoryDataSet
import cc.datafabric.exchange.cim.repository.common.dto.RepositoryEntity
import cc.datafabric.exchange.cim.repository.common.dto.RepositoryLink

object DataSetMapper {


    private fun fillDataSet(dataSet: DataSet, repositoryDataSet: RepositoryDataSet) {
        repositoryDataSet.entities.forEach { repositoryEntity ->
            val entity = dataSet.getOrCreate(repositoryEntity.id, repositoryEntity.type)
            entity.code = repositoryEntity.code
            entity.baseCode = repositoryEntity.baseCode
            repositoryEntity.attributes.forEach { attr->
                entity.setPropertyValue(attr.key.split(".").last(), attr.value)
            }
        }
        repositoryDataSet.links.forEach { repositoryLink ->
            val entity = dataSet.get(repositoryLink.fromId)
            val value = dataSet.get(repositoryLink.toId)

            val predicate = repositoryLink.predicate.split(".").last()
            entity!!.addLink(predicate, value!!)
        }
    }

    fun toDataSet(repositoryDataSet: RepositoryDataSet): DataSet {
        val dataSet =  DataSet()
        fillDataSet(dataSet,repositoryDataSet)
        return dataSet
    }

    private fun copyExtraProps (source:ModelObject, target: RepositoryEntity){
        source.extraProperties.forEach {
            val item = it.value
            val value = if (item is ModelObject) {
                item.id
            } else {
                item
            }
            target.extraAttributes[it.key] = value
        }
    }
    fun toRepositoryDataSet(dataSet: DataSet): RepositoryDataSet {
        val entities =  mutableListOf<RepositoryEntity>()
        val links =  mutableListOf<RepositoryLink>()
        dataSet.objects.forEach { obj->
            val repositoryEntity = RepositoryEntity(obj.id, ReflectionUtils.getObjectClassName(obj))
            repositoryEntity.code = obj.code
            repositoryEntity.baseCode = obj.baseCode
            repositoryEntity.extraAttributes
            copyExtraProps(obj,repositoryEntity)
            entities.add(repositoryEntity)
            ReflectionUtils.getNonLinkProperties(obj).forEach { prop->
                repositoryEntity.attributes[ReflectionUtils.getPropFullName(prop)] =  ReflectionUtils.getPropertyValue(obj,prop)
            }
            ReflectionUtils.getLinkProperties(obj).forEach { prop->
                val value =  ReflectionUtils.getLinkPropValue(obj,prop)
                if (value!=null){
                    val repositoryLink = RepositoryLink(
                        fromType = ReflectionUtils.getObjectClassName(obj),
                        fromId = obj.id,
                        predicate = ReflectionUtils.getPropFullName(prop),
                        toType = ReflectionUtils.getObjectClassName(value),
                        toId = value.id
                    )
                    links.add(repositoryLink)
                }
            }

            ReflectionUtils.getLinksProperties(obj).forEach { prop->
                val values =  ReflectionUtils.getLinksPropValue(obj,prop)
                values.forEach{value->
                    val repositoryLink = RepositoryLink(
                        fromType = ReflectionUtils.getObjectClassName(obj),
                        fromId = obj.id,
                        predicate = ReflectionUtils.getPropFullName(prop),
                        toType = ReflectionUtils.getObjectClassName(value),
                        toId = value.id
                    )
                    links.add(repositoryLink)
                }
            }
        }
        val repositoryDataSet = RepositoryDataSet()
        repositoryDataSet.entities =  entities.toList()
        repositoryDataSet.links = links.toList()
        return  repositoryDataSet
    }



}