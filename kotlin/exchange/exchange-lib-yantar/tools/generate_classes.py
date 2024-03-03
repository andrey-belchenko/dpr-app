import os
import xml.etree.ElementTree as ET


class ClassInfo:
    name: str
    subClassOf: "ClassInfo" = None
    properties: list["PropInfo"]
    isDefined: bool
    isPropsLoaded: bool

    def nameInPascalCase(self):
        return toPascalCase(self.name)


class PropInfo:
    name: str
    isArray: bool
    isRequired: bool = False
    range: "ClassInfo"
    inverseOf: str = None

    def nameInCamelCase(self):
        return toCamelCase(self.name)


namespaces = {
    "owl": "http://www.w3.org/2002/07/owl#",
    "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
    "j.2": "http://iec.ch/TC57/1999/rdf-schema-extensions-19990926#",
}

# inversion = [
#     ["EquipmentContainer.Equipments", "Equipment.EquipmentContainer"],
#     ["IdentifiedObject.childObjects", "IdentifiedObject.ParentObject"]
# ]


iriPrefix = "http://ontology.adms.ru/UIP/md/2021-1#"


def ns(prefix):
    return "{" + namespaces[prefix] + "}"


def parseXml(filePath):
    tree = ET.parse(filePath)
    root = tree.getroot()
    return root


def getNameFromElementIri(cls: ET.Element) -> str:
    attrVal = cls.attrib.get(ns("rdf") + "about")
    if attrVal == None:
        attrVal = cls.attrib.get(ns("rdf") + "resource")
    if attrVal == None:
        return None
    vals = attrVal.split("#")[1].split(".")
    return vals[len(vals) - 1]


# def getNameFromPropIri(prop: ET.Element) -> str:
#     attrVal = prop.attrib.get(ns("rdf") + "about")
#     if (attrVal == None):
#         attrVal = prop.attrib.get(ns("rdf") + "resource")
#     if (attrVal == None):
#         return None
#     return attrVal.split('#')[1]


def getClassInfoList(root: ET.Element, names: list[str]) -> list[ClassInfo]:
    classInfoList: list[ClassInfo] = []
    for name in names:
        getClassInfo(root, name, classInfoList, names, True, False)

    # Манипуляции чтобы собрать только нужные классы но с учетом родительских
    namesWithSuperClasses: list[str] = []
    for classInfo in classInfoList:
        namesWithSuperClasses.append(classInfo.name)

    for name in namesWithSuperClasses:
        getClassInfo(root, name, classInfoList, namesWithSuperClasses, True, True)

    clearClassInfoList: list[ClassInfo] = []
    for classInfo in classInfoList:
        if classInfo.isPropsLoaded:
            clearClassInfoList.append(classInfo)

    for classInfo in clearClassInfoList:
        for propInfo in classInfo.properties:
            # if (propInfo.name=="Switches"):
            #     None
            if propInfo.inverseOf != None and propInfo.range.isDefined:
                rangeClassInfo = list(
                    filter(
                        lambda it: it.name == propInfo.range.name, clearClassInfoList
                    )
                )[0]
                inversePropInfo = list(
                    filter(
                        lambda it: propInfo.range.name + "." + it.name
                        == propInfo.inverseOf,
                        rangeClassInfo.properties,
                    )
                )[0]
                inversePropInfo.inverseOf = classInfo.name + "." + propInfo.name

    return clearClassInfoList


# def getInverseOf(name: str):
#     for item in inversion:
#         if item[0] == name:
#             return item[1]
#         if item[1] == name:
#             return item[0]


def getClassInfo(
    root: ET.Element,
    name,
    classInfoList: list[ClassInfo],
    requiredClasses: list[str],
    withParents: bool,
    withProps: bool,
) -> ClassInfo:
    classInfo = next((it for it in classInfoList if it.name == name), None)
    if classInfo != None and (classInfo.isPropsLoaded or not withProps):
        return classInfo
    foundCls = root.findall(f'./owl:Class[@rdf:about="{iriPrefix+name}"]', namespaces)
    if classInfo == None:
        classInfo = ClassInfo()
    classInfo.properties = []
    classInfo.name = name
    classInfoList.append(classInfo)
    if len(foundCls) == 0:
        # classInfo.name = name
        classInfo.isDefined = False
        classInfo.isPropsLoaded = True
        print(classInfo.name)
        return classInfo
    classInfo.isDefined = True
    cls = foundCls[0]
    # classInfo.name = getNameFromElementIri(cls)
    if withParents:
        for subClassOf in cls.findall(f"./rdfs:subClassOf", namespaces):
            baseName = getNameFromElementIri(subClassOf)
            if baseName == None:
                baseCls = subClassOf.findall(f"./owl:Class", namespaces)[0]
                baseName = getNameFromElementIri(baseCls)
            classInfo.subClassOf = getClassInfo(
                root, baseName, classInfoList, requiredClasses, True, withProps
            )
    classInfo.isPropsLoaded = False
    if withProps:
        classInfo.isPropsLoaded = True
        for prop in root.findall(
            f'./*/rdfs:domain[@rdf:resource="{iriPrefix+name}"]/..', namespaces
        ):
            range = prop.findall(f"./rdfs:range", namespaces)[0]
            rangeName = getNameFromElementIri(range)
            multiplicity = prop.findall(f"./j.2:multiplicity", namespaces)[0]
            multiplicityVal = (
                multiplicity.attrib.get(ns("rdf") + "resource")
                .split("#")[1]
                .split(":")[1]
            )
            multiplicityVals = multiplicityVal.split("..")
            multiplicityMax = multiplicityVals[len(multiplicityVals) - 1]
            multiplicityMin = multiplicityVals[0]

            rangeClass = getClassInfo(
                root, rangeName, classInfoList, requiredClasses, False, False
            )
            # if (multiplicityMax == "1" or (rangeName in requiredClasses)):
            # not rangeClass.isDefined это примитивные типы string bool итд
            if (rangeName in requiredClasses) or (not rangeClass.isDefined):

                propName = getNameFromElementIri(prop)
                inverseOf = prop.findall(
                    "./owl:inverseOf/owl:ObjectProperty", namespaces
                )
                if len(inverseOf) == 0:
                    inverseOf = prop.findall("./owl:inverseOf", namespaces)
                inverseOfVal = None
                if len(inverseOf) > 0:
                    inverseOfVal = (
                        rangeClass.name + "." + getNameFromElementIri(inverseOf[0])
                    )
                # print(name+"."+propName)
                propInfo = next(
                    (it for it in classInfo.properties if it.name == propName), None
                )
                if (
                    propInfo == None
                ):  # почему то некоторые свойства залетают дважды, не стал искать причину поставил проверку
                    propInfo = PropInfo()
                    classInfo.properties.append(propInfo)
                    propInfo.name = propName
                    propInfo.range = rangeClass
                    propInfo.isArray = multiplicityMax != "1"
                    # getInverseOf(name+"."+propName)
                    propInfo.inverseOf = inverseOfVal
                    if not propInfo.isArray:
                        propInfo.isRequired = multiplicityMin != "0"
    return classInfo


def toPascalCase(string):
    if string[0].isupper():
        return string
    pascal_case_string = ""
    for char in string:
        if char.isupper():
            pascal_case_string += " " + char
        else:
            pascal_case_string += char
    return pascal_case_string.title().replace(" ", "")


def toCamelCase(string):
    if not string[0].isupper():
        return string
    camel_case_string = string[0].lower() + string[1:]
    return camel_case_string


def generateKotlinClasses(classInfoList: list[ClassInfo], folder: str):
    baseClassName = "ModelObject"
    for file in os.listdir(folder):
        path = os.path.join(folder, file)
        if not os.path.isdir(path):
            os.remove(path)

    systemClassMap = {
        "string": "String",
        "boolean": "Boolean",
        "integer": "Int",
        "float": "Float",
    }

    def refTypeName(range: str):
        if range in systemClassMap:
            return systemClassMap[range]
        return range

    package = "cc.datafabric.exchange"
    sysPackage = f"{package}.cim.model"
    dataPackage = f"{package}.scenario.yantar.model.data"
    for classInfo in classInfoList:
        if classInfo.name not in systemClassMap and (classInfo.isDefined):
            # if classInfo.name=="Switch":
            #     None
            headerText = "//generated from profile\n"
            headerText += f"package {dataPackage}\n"
            headerText += "\n"

            hasLinkProp = False
            hasLinksProp = False
            hasValProp = False

            baseClass = baseClassName
            if classInfo.subClassOf != None and classInfo.subClassOf.name != "Entity":
                baseClass = classInfo.subClassOf.name
            else:
                headerText += f"import {sysPackage}.ModelObject\n"
            classText = '@Suppress("PropertyName", "unused")\n'
            classText += f"open class {classInfo.name} : {baseClass}()"
            if len(classInfo.properties) > 0:
                classText += " {\n"
                checkTypes = []
                for propInfo in classInfo.properties:
                    # if (propInfo.name!="Name"): # есть name и Name Kotlin такое не позволяет
                    type = refTypeName(propInfo.range.name)
                    typeAlias = type
                    for propInfo1 in classInfo.properties:
                        if typeAlias == propInfo1.name:
                            typeAlias = type + "Class"
                            if type not in checkTypes:
                                headerText += (
                                    f"import {dataPackage}.{type} as {typeAlias}\n"
                                )
                                checkTypes.append(type)
                            break

                    propDeclaration = ""

                    # if classInfo.name=="Switch" and propInfo.name == "LineSpan":
                    #     None
                    if propInfo.range.isDefined:
                        inverseParam = ""
                        if propInfo.inverseOf != None:
                            inverseField = propInfo.inverseOf.split(".")[1]
                            inverseParam = (
                                f"inverseProperty = {typeAlias}::{inverseField}"
                            )
                        if propInfo.isArray:
                            propDeclaration = f"val {propInfo.name}: Links<{typeAlias}> by LinksDelegate({inverseParam})"
                            if not hasLinksProp:
                                headerText += f"import {sysPackage}.Links\n"
                                headerText += f"import {sysPackage}.LinksDelegate\n"
                                hasLinksProp = True
                        else:
                            propDeclaration = f"var {propInfo.name}: {typeAlias}? by LinkDelegate({inverseParam})"
                            if not hasLinkProp:
                                headerText += f"import {sysPackage}.LinkDelegate\n"
                                hasLinkProp = True

                    else:
                        if propInfo.range.name in systemClassMap:
                            propDeclaration = (
                                f"var {propInfo.name}: {type}? by ValueDelegate()"
                            )
                            if not hasValProp:
                                headerText += f"import {sysPackage}.ValueDelegate\n"
                                hasValProp = True
                        else:
                            print(
                                "skipped "
                                + classInfo.name
                                + "."
                                + propInfo.name
                                + ":"
                                + propInfo.range.name
                            )
                    if propDeclaration != "":
                        classText += "    " + propDeclaration + "\n"
                classText += "}"
            path = os.path.join(folder, f"{classInfo.name}.kt")
            with open(path, "w", encoding="utf8") as outfile:
                outfile.write(headerText + "\n" + classText)
            # print(path)
            # print(text)


dir = os.path.dirname(os.path.abspath(__file__))
schemaPath = os.path.join(dir, "АСТУ.xml")

trgFolderPath = os.path.join(
    dir, "..", "src/main/kotlin/cc/datafabric/exchange/scenario/yantar/model/data"
)

root = parseXml(schemaPath)
classInfoList = getClassInfoList(
    root,
    [
        "Substation",
        "InfSupplyCenter",
        # "Disconnector",
        # "Breaker",
        # "GroundDisconnector",
        # "Recloser",
        # "ACLineSegment",
        # "ConnectivityNode",
        # "Terminal",
        # "AccountPartLine",
        # "Tower",
        # "LineSpan",
        # "SubGeographicalRegion",
        # "BaseVoltage",
    ],
)
generateKotlinClasses(
    classInfoList,
    trgFolderPath,
)


# <owl:inverseOf>
#       <owl:ObjectProperty rdf:about="http://ontology.adms.ru/UIP/md/2021-1#TechConnectSubAbon.techConnectionSchemes"/>
#  </owl:inverseOf>

# <owl:inverseOf rdf:resource="http://ontology.adms.ru/UIP/md/2021-1#Tower.accountPartLine"/>
#  <owl:Class rdf:about="http://ontology.adms.ru/UIP/md/2021-1#ACLineSegmentPhase">
#     <rdfs:subClassOf>
#       <owl:Class rdf:about="http://ontology.adms.ru/UIP/md/2021-1#PowerSystemResource"/>
#     </rdfs:subClassOf>
#     <rdfs:label>ACLineSegmentPhase</rdfs:label>
#     <rdfs:isDefinedBy rdf:resource="http://ontology.adms.ru/UIP/md/2021-1#"/>
#     <rdfs:comment>Одиночный провод сегмента линии переменного тока.</rdfs:comment>
#     <j.6:importFromOntology rdf:resource="http://gost.ru/2019/schema-cim01#"/>
#     <j.6:importFromClass rdf:resource="http://iec.ch/TC57/2014/CIM-schema-cim16#ACLineSegmentPhase"/>
#   </owl:Class>

#  <owl:Class rdf:about="http://ontology.adms.ru/UIP/md/2021-1#Line">
#     <rdfs:subClassOf rdf:resource="http://ontology.adms.ru/UIP/md/2021-1#EquipmentContainer"/>
#     <rdfs:label>Line</rdfs:label>
#     <rdfs:isDefinedBy rdf:resource="http://ontology.adms.ru/UIP/md/2021-1#"/>
#     <j.6:lastUser>a.nikitin</j.6:lastUser>
#     <j.6:lastUpdate>2022-08-25T16:49:41.626Z</j.6:lastUpdate>
#     <j.6:importFromOntology rdf:resource="http://gost.ru/2019/schema-cim01#"/>
#     <j.6:importFromClass rdf:resource="http://iec.ch/TC57/2014/CIM-schema-cim16#Line"/>
#   </owl:Class>

#  <owl:ObjectProperty rdf:about="http://ontology.adms.ru/UIP/md/2021-1#TechConnectionSheme.SchemeLine">
#     <rdfs:range rdf:resource="http://ontology.adms.ru/UIP/md/2021-1#Line"/>
#     <rdfs:label>SchemeLine</rdfs:label>
#     <rdfs:isDefinedBy rdf:resource="http://ontology.adms.ru/UIP/md/2021-1#"/>
#     <rdfs:domain rdf:resource="http://ontology.adms.ru/UIP/md/2021-1#TechConnectionSheme"/>
#     <j.6:PropertyOrder rdf:datatype="http://www.w3.org/2001/XMLSchema#integer"
#     >260</j.6:PropertyOrder>
#     <j.2:multiplicity rdf:resource="http://iec.ch/TC57/1999/rdf-schema-extensions-19990926#M:0..1"/>
#   </owl:ObjectProperty>

# <owl:DatatypeProperty rdf:about="http://ontology.adms.ru/UIP/md/2021-1#IdentifiedObject.name">
#     <rdfs:range rdf:resource="http://www.w3.org/2001/XMLSchema#string"/>
#     <rdfs:label>name</rdfs:label>
#     <rdfs:isDefinedBy rdf:resource="http://ontology.adms.ru/UIP/md/2021-1#"/>
#     <rdfs:domain rdf:resource="http://ontology.adms.ru/UIP/md/2021-1#IdentifiedObject"/>
#     <j.6:PropertyOrder rdf:datatype="http://www.w3.org/2001/XMLSchema#integer"
#     >60</j.6:PropertyOrder>
#     <j.6:importFromProperty rdf:resource="http://iec.ch/TC57/2014/CIM-schema-cim16#IdentifiedObject.name"/>
#     <j.2:multiplicity rdf:resource="http://iec.ch/TC57/1999/rdf-schema-extensions-19990926#M:0..1"/>
#   </owl:DatatypeProperty>

# <owl:ObjectProperty rdf:about="http://ontology.adms.ru/UIP/md/2021-1#Asset.PowerSystemResources">
#     <rdfs:range rdf:resource="http://ontology.adms.ru/UIP/md/2021-1#PowerSystemResource"/>
#     <rdfs:label>PowerSystemResources</rdfs:label>
#     <rdfs:isDefinedBy rdf:resource="http://ontology.adms.ru/UIP/md/2021-1#"/>
#     <rdfs:domain rdf:resource="http://ontology.adms.ru/UIP/md/2021-1#Asset"/>
#     <j.6:PropertyOrder rdf:datatype="http://www.w3.org/2001/XMLSchema#integer"
#     >100</j.6:PropertyOrder>
#     <j.6:importFromProperty rdf:resource="http://iec.ch/TC57/2014/CIM-schema-cim16#Asset.PowerSystemResources"/>
#     <j.2:multiplicity rdf:resource="http://iec.ch/TC57/1999/rdf-schema-extensions-19990926#M:0..n"/>
#   </owl:ObjectProperty>
