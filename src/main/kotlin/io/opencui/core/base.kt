package io.opencui.core

import com.fasterxml.jackson.annotation.JsonProperty
import io.opencui.channel.IChannel
import io.opencui.core.da.DialogActRewriter
import io.opencui.core.user.UserInfo
import io.opencui.du.*
import io.opencui.du.DUMeta.Companion.parseExpressions
import io.opencui.serialization.*
import io.opencui.sessionmanager.ChatbotLoader
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.*


enum class ValueOperator {
    And,
    Not,
    Or,
    EqualTo,
    LessThan,
    GreaterThan,
    LessThanOrEqualTo,
    GreaterThanOrEqualTo
}

data class SlotValue(
  @JsonProperty
  val slot: String? = null,
  @JsonProperty
  val value: Any? = null
)


/**
 *
 * There are two aspects of type systems: one is on type sides, and another is on the
 * translation from language to schema. On type side, the difference is between is-a
 * and can_be_cast_as, on the translation side, it is rewritten to:
 * how long is your {symptom} ?
 * or make the following not frame/intent trigger
 * start from {beijing}.
 *
 * Without rewriting the training phrase, "how long did you have your {symptom}?" is not useful
 * for headache, stomachache etc.
 *
 */
interface FillBuilder : (ParamPath) -> FrameFiller<*>, Serializable

interface PolymorphicFrameGenerator: (String) -> IFrame?, Serializable

fun createFrameGenerator(session: UserSession, interfaceClassName: String) = object: PolymorphicFrameGenerator {
    override operator fun invoke(type: String): IFrame? {
        val interfaceKClass = session.findKClass(interfaceClassName) ?: return null
        val packageName = type.substringBeforeLast(".", interfaceClassName.substringBeforeLast("."))
        val simpleType = type.substringAfterLast(".")
        val frame = session.construct(packageName, simpleType, session)
        return if (interfaceKClass.isInstance(frame)) frame else null
    }
}

/**
 * One should be able to access connection, and even session. The IService contains a set of functions.
 * Service is also attached to session, just like frame.
 */
interface IEntity : Serializable{
    var value: String
    var origValue: String?
}

/**
 * For value disambiguation, we need to expose some information for the generic implementation
 * for the slot that we bind dynamically.
 * Builder can also do slotNames.random() and typeNames.random().
 */
interface IFrame : Serializable {
    var session: UserSession?
    fun annotations(path: String): List<Annotation> = listOf()

    fun createBuilder(p: KMutableProperty0<out Any?>? = null): FillBuilder


    // slot "this" is a special slot which indicates searching for frame confirmation
    fun searchConfirmation(slot: String): IFrame? {
        return null
    }

    fun searchStateUpdateByEvent(event: String): IFrameBuilder? {
        return null
    }
}

inline fun <reified T : Annotation> IFrame.find(rpath: String): T? =
    annotations(rpath).firstOrNull { it is T && it.switch() } as T?

inline fun <reified T : Annotation> IFrame.findAll(rpath: String): List<T> =
    annotations(rpath).filter { it is T && it.switch() }?.map { it as T } ?: listOf()

interface IIntent : IFrame {
    // TODO(xiaobo, xiaoyun): all the filling related property should be in filler instead of frame, ideally.
    fun searchResponse(): Action? {
        return null
    }
}

interface IKernelIntent: IIntent


@Throws(NoSuchMethodException::class)
fun invokeMethodByReflection(receiver: Any, funName: String, vararg params: Any?): Any? {
    val kClass = receiver::class
    val method = kClass.memberFunctions.firstOrNull { it.name == funName && it.parameters.size == params.size + 1 } ?: throw NoSuchMethodException("no such method : $funName") // param of method include receiver
    return method.call(receiver, *params)
}

@Throws(NoSuchPropertyException::class)
fun getPropertyValueByReflection(receiver: Any, propertyName: String): Any? {
    val kClass = receiver::class
    val property = kClass.members.firstOrNull { it.name == propertyName } as? KMutableProperty1<Any, *> ?: throw NoSuchPropertyException()
    return property.get(receiver)
}


// This is used to pass the runtime configure, information needed for all agents.
object RuntimeConfig {
    val configures = mutableMapOf<KClass<*>, Any>()
    inline fun <reified T: Any> put(key: KClass<*>, value: T) {
        if (configures.containsKey(key)) {
            Dispatcher.logger.info("$key is configured twice, was ${configures[key]} now $value")
        }
        configures[key] = value
    }

    inline fun <reified T: Any> update(key: KClass<*>, value: T) {
        configures[key] = value
    }

    inline fun <reified T: Any> get(key: KClass<*>) : T {
        return configures[key] as T
    }
}


data class RoutingInfo(val id: String, val intentsDesc: List<String>)


interface BotInfo: Serializable {
    val fullName: String
    val lang: String
    val branch: String
}

fun master(lang: String = "*") : BotInfo {
    return object : BotInfo {
        override val fullName =  Dispatcher.botPrefix!!
        override val lang = lang
        override val branch = "master" }
}
fun botInfo(fullName: String) : BotInfo {
    return object : BotInfo {
        override val fullName =  fullName
        override val lang = "*"
        override val branch = "master" }
}
fun botInfo(org: String, bot: String) : BotInfo {
    return object : BotInfo {
        override val fullName =  "${org}.${bot}"
        override val lang = "*"
        override val branch = "master" }
}
fun botInfo(fullName: String, lang: String, branch: String) : BotInfo {
    return object : BotInfo {
        override val fullName =  fullName
        override val lang = lang
        override val branch = branch }
}
fun botInfo(org: String, bot: String, lang: String, branch: String) : BotInfo {
    return object : BotInfo {
        override val fullName =  "${org}.${bot}"
        override val lang = lang
        override val branch = branch }
}

interface Component {
    val orgName: String
    val agentName: String
    val agentBranch: String
    val agentVersion: String
    val agentLang: String
    val timezone: String
}

/**
 * The chatbot implementation will be used to hold the data/filler together.
 * TODO: why this is NOT implement AgentMeta?
 */
abstract class IChatbot : Component {
    abstract val duMeta: DUMeta

    override val orgName: String = duMeta.getOrg()
    override val agentName: String = duMeta.getLabel()
    override val agentBranch: String = duMeta.getBranch()
    override val agentVersion: String = duMeta.getVersion()
    override val agentLang: String = duMeta.getLang()
    override val timezone: String = duMeta.getTimezone()

    // Do we have support connected behind bot?
    abstract val stateTracker: StateTracker

    // This is used to host extension managers.
    val extensions = mutableMapOf<KClass<*>, ExtensionManager<*>>()

    // This is used for hosting dialog act rewrite rule.
    abstract val rewriteRules: List<KClass<out DialogActRewriter>>

    // This is designed for routing conversation to right department when needed.
    abstract val routing: Map<String, RoutingInfo>

    fun getChannel(type: String, label: String) : IChannel? {
        return extensions[IChannel::class]?.get(label) as IChannel?
    }

    inline fun <reified T> getExtension(label: String): T? {
       return extensions[T::class]?.get(label) as T?
    }
    inline fun <reified T> getExtensionManager(): ExtensionManager<*> {
       return extensions[T::class]!!
    }

    inline fun <reified T> getConfiguration(label: String): Configuration? {
        return Configuration.get(T::class.qualifiedName!!, label)
    }

    fun createUserSession(channelType: String, user: String, channelLabel: String?): UserSession{
        return UserSession(UserInfo(channelType, user, channelLabel), chatbot = this)
    }

    inline fun <reified T:IExtension> buildManager(init: ExtensionManager<T>.() -> Unit) : ExtensionManager<T> {
        val manager = ExtensionManager<T>(T::class.qualifiedName!!)
        manager.init()
        return manager
    }

    open fun recycle() {
    }

    fun getLoader() : ClassLoader = ChatbotLoader.findClassLoader(botInfo(orgName, agentName, agentLang, agentBranch))

    operator fun invoke(p1: String, session: UserSession, packageName: String? = null): FillBuilder? {
        // hardcode for clean session
        val revisedPackageName = packageName ?: this.javaClass.packageName
        return session.construct(revisedPackageName, p1, session)?.createBuilder()
    }

    companion object {
        val ExpressionPath = "expression.json"
        val EntityPath = "entity.json"
        val EntityMetaPath = "entity_meta.json"
        val SlotMetaPath = "slot_meta.json"
        val AliasPath = "alias.json"

        fun parseByFrame(agentJsonExpressions: String): JsonArray {
            val root = Json.parseToJsonElement(agentJsonExpressions) as JsonObject
            return root["expressions"]!! as JsonArray
        }

        fun parseEntityToMapByNT(entity: String, entries: String): Map<String, List<String>> {
            println("processing $entity with $entries")
            // content are encoded by newline and tab.
            val lines = entries.split("\n").map{ it.split("\t") }

            val nempties = lines.count{ it[0] == ""}
            val nnemptties = lines.count{ it[0] != ""}

            assert (nempties == 0 || nnemptties == 0)

            val map: MutableMap<String, List<String>> = mutableMapOf()
            for (l in lines.withIndex()) {
                val norm =  if (nnemptties != 0) l.value[0] else "${entity}.${l.index}"
                map[norm] = l.value.subList(1, l.value.size)
            }
            return map
        }

        fun loadDUMeta(classLoader: ClassLoader, org: String, agent: String, lang: String, branch: String, version: String, timezone: String = "america/los_angeles"): DUMeta {
            return object : JsonDUMeta() {
                override val entityMetas = Json.decodeFromString<Map<String, EntityMeta>>(
                    classLoader.getResourceAsStream(EntityMetaPath).bufferedReader(Charsets.UTF_8).use { it.readText() })
                val agentEntities = Json.decodeFromString<Map<String, String>>(
                    classLoader.getResourceAsStream(EntityPath).bufferedReader(Charsets.UTF_8).use { it.readText() })
                override val slotMetaMap = Json.decodeFromString<Map<String, List<DUSlotMeta>>>(
                    classLoader.getResourceAsStream(SlotMetaPath).bufferedReader(Charsets.UTF_8).use { it.readText() })
                override val aliasMap = Json.decodeFromString<Map<String, List<String>>>(
                    classLoader.getResourceAsStream(AliasPath).bufferedReader(Charsets.UTF_8).use { it.readText() })
                val entityContentMap: MutableMap<String, Map<String, List<String>>> = mutableMapOf()

                init {
                    for (entity in agentEntities.entries) {
                        entityContentMap[entity.key] = parseEntityToMapByNT(entity.key, entity.value)
                    }
                }

                override fun getSubFrames(fullyQualifiedType: String): List<String> { return subtypes[fullyQualifiedType] ?: emptyList() }

                override fun getOrg(): String = org
                override fun getLang(): String = lang
                override fun getLabel(): String = agent
                override fun getVersion(): String = version
                override fun getBranch(): String = branch
                override fun getTimezone(): String = timezone

                override fun getEntityInstances(name: String): Map<String, List<String>> {
                    return entityContentMap[name] ?: mapOf()
                }

                override val expressionsByFrame: Map<String, List<Expression>>
                    get() = parseExpressions(
                        parseByFrame(classLoader.getResourceAsStream(ExpressionPath).bufferedReader(Charsets.UTF_8).use { it.readText() }), this)
            }
        }

        fun loadDUMetaDsl(langPack: LangPack, classLoader: ClassLoader, org: String, agent: String, lang: String, branch: String, version: String, timezone: String = "america/los_angeles"): DUMeta {
            return object : DslDUMeta() {
                override val entityTypes = langPack.entityTypes
                override val slotMetaMap = langPack.frameSlotMetas
                override val aliasMap = langPack.typeAlias

                init {
                    val surroundings = extractSlotSurroundingWords(expressionsByFrame, LanguageAnalyzer.get(lang, stop=false)!!)
                    for ((frame, slots) in slotMetaMap) {
                        for (slot in slots) {
                            slot.prefixes = surroundings.first["$frame:${slot.label}"]
                            slot.suffixes = surroundings.second["$frame:${slot.label}"]
                        }
                    }
                }

                override fun getSubFrames(fullyQualifiedType: String): List<String> { return subtypes[fullyQualifiedType] ?: emptyList() }

                override fun getOrg(): String = org
                override fun getLang(): String = lang
                override fun getLabel(): String = agent
                override fun getVersion(): String = version
                override fun getBranch(): String = branch
                override fun getTimezone(): String = timezone

                override fun getEntityInstances(name: String): Map<String, List<String>> {
                    return langPack.entityTypes[name]!!.entities
                }

                override val expressionsByFrame: Map<String, List<Expression>>
                    get() = parseExpressions(Json.makeArray(langPack.frames), this)
            }
        }

    }
}
