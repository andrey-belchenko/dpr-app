package cc.datafabric.exchange.cim.model

import org.reflections.Reflections
import kotlin.reflect.*
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

object ReflectionUtils {

    private fun packageName(): String {
        return ModelObject::class.java.packageName.replace(".cim.model", ".scenario.model.data")
    }

    private var classes: MutableMap<String, KClass<*>> = mutableMapOf()
    fun createObject(className: String): ModelObject {
        if (!classes.containsKey(className)) {
            classes[className] = Class.forName(packageName() + "." + className).kotlin
        }
        val clazz = classes[className]!!
        return createObject(clazz)
    }

    private fun createObject(clazz: KClass<*>): ModelObject {
        return clazz.createInstance() as ModelObject
    }


    fun getPropertyValue(obj: ModelObject, property: KProperty<*>): Any? {
        return property.getter.call(obj)
    }

    private fun setLinkPropertyValue(obj: ModelObject, property: KProperty<*>, value: ModelObject?) {
        (property as KMutableProperty).setter.call(obj, value)
    }


    fun setPropertyValue(obj: ModelObject, fieldName: String, value: Any?): Boolean {
        val property = obj::class.members.find { it.name == fieldName } ?: return false
        val convertedValue = convertValue(value, property.returnType)
        (property as KMutableProperty).setter.call(obj, convertedValue)
        return true
    }

    private fun convertValue(value: Any?, targetType: KType): Any? {
        if (value == null) return null
        return when (targetType.classifier) {
            Int::class -> value.toString().toDouble().toInt()
            Long::class -> value.toString().toDouble().toLong()
            Double::class -> value.toString().toDouble()
            Float::class -> value.toString().toFloat()
            Boolean::class -> value.toString().toBoolean()
            String::class -> value.toString()
            else -> throw IllegalArgumentException("Unsupported target type: $targetType")
        }
    }


    fun addLink(obj: ModelObject, fieldName: String, value: ModelObject): Boolean {
        val property = obj::class.members.find { it.name == fieldName } ?: return false
        addLink(obj, property as KProperty<*>, value)
        return true
    }

    fun addLink(obj: ModelObject, property: KProperty<*>, value: ModelObject) {
        if (property.returnType.classifier == Links::class) {
            val links = getLinksPropValue(obj, property)
            links.add(value)
        } else {
            setLinkPropertyValue(obj, property, value)
        }
    }

    private fun isLinksProperty(property: KProperty<*>): Boolean {
        return property.returnType.classifier == Links::class
    }

    private fun isLinkProperty(property: KProperty<*>): Boolean {
        return (property.returnType.classifier as KClass<*>).isSubclassOf(ModelObject::class)
    }

    fun removeLink(obj: ModelObject, property: KProperty<*>, value: ModelObject) {
        if (isLinksProperty(property)) {
            val links = getLinksPropValue(obj, property)
            links.remove(value)
        } else {
            setLinkPropertyValue(obj, property, null)
        }
    }


    fun clearObjectLinks(obj: ModelObject) {
        obj::class.members.forEach {
            if (it is KProperty) {
                if (isLinksProperty(it)) {
                    getLinksPropValue(obj, it).clear()
                } else if (isLinkProperty(it)) {
                    setLinkPropertyValue(obj, it, null)
                }
            }
        }
    }

    private var classSubclasses: MutableMap<String, Iterable<String>> = mutableMapOf()
    fun getClassSubclasses(cls: KClass<*>): Iterable<String> {
        val className = cls.simpleName
        if (!classSubclasses.containsKey(cls.simpleName)) {
            val list = mutableListOf<String>()
            val reflections = Reflections(packageName())
            reflections.getSubTypesOf(cls.java).forEach {
                val name = it.simpleName
                list.add(name)
            }
            list.add(className!!)
            classSubclasses[className] = list
        }
        return classSubclasses[className]!!
    }


    fun getLinkProperties(obj: ModelObject): Iterable<KProperty<*>> {
        return obj::class.members.filter { it is KProperty<*> && isLinkProperty(it) }.map { it as KProperty<*> }
    }

    fun getLinksProperties(obj: ModelObject): Iterable<KProperty<*>> {
        return obj::class.members.filter { it is KProperty<*> && isLinksProperty(it) }.map { it as KProperty<*> }
    }

    fun getNonLinkProperties(obj: ModelObject): Iterable<KProperty<*>> {
        return obj::class.members.filter {
            it is KProperty<*> &&
                    !isLinksProperty(it) &&
                    !isLinkProperty(it) &&
                    it.javaField!!.declaringClass!= ModelObject::class.java
        }.map { it as KProperty<*> }
    }
    fun getLinkPropValue(obj: ModelObject, property: KProperty<*>): ModelObject? {
        return getPropertyValue(obj, property) as ModelObject?
    }

    @Suppress("UNCHECKED_CAST")
    fun getLinksPropValue(obj: ModelObject, property: KProperty<*>): Links<ModelObject> {
        return getPropertyValue(obj, property) as Links<ModelObject>
    }

    fun getPropFullName(property: KProperty<*>): String {
        return property.javaField!!.declaringClass.simpleName + "." + property.name
    }

    fun getObjectClassName(obj: ModelObject): String {
        return obj.javaClass.simpleName
    }

    fun getInverseProperty(property: KProperty<*>): KProperty<*>? {
        property.apply { isAccessible = true }
        val declaringClass: Class<*> = property.javaField!!.declaringClass
        val instance = declaringClass.kotlin.createInstance() as ModelObject
        @Suppress("UNCHECKED_CAST")
        val delegate = (property as KMutableProperty1<ModelObject, *>).getDelegate(instance)
        val linkDelegate = (delegate as LinkDelegate<*>)
        return linkDelegate.getInverseProperty()
    }


}