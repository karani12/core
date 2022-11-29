package io.opencui.test

import io.opencui.core.*
import io.opencui.core.Annotation
import io.opencui.serialization.*
import kotlin.reflect.KMutableProperty0
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import io.opencui.core.da.*
import java.io.Serializable

data class PayMethod(@get:JsonIgnore var value: String): Serializable {
    var origValue: String? = null
    @JsonValue
    override fun toString() : String = value

    fun concreteMethod(): String {
        return if (value == "visa") "associate value" else "wrong value"
    }

    companion object {

        val normalizedFormMap = Agent.duMeta.getEntityInstances(PayMethod::class.qualifiedName!!)

        fun getAllInstances(): List<PayMethod> {
            return normalizedFormMap.map { PayMethod(it.key) }
        }

        fun createRecFrame(session: UserSession, target: IFrame? = null, slot: String? = null): PagedSelectable<PayMethod> {
            return PagedSelectable(session, { getAllInstances() },  { PayMethod::class },
                {offers -> SlotOffer(offers, "this", "io.opencui.test.PayMethod",
                        simpleTemplates(Prompt {
                            with(session) {
                                """We have following ${offers.size} choices for PayMethod : ${
                                    offers.joinToString(
                                        ", "
                                    ) { it.name() }
                                }."""
                            }
                        }))
                },
                target = target, slot = slot, implicit = false)
        }
    }
}

interface ISymptom: IFrame {
    var duration: Int?
    var cause: String?
}

data class Symptom(@JsonIgnore @Transient override var session: UserSession? = null) : IFrame, ISymptom {
    override var duration: Int? = null
    override var cause: String? = null

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: Symptom? = this@Symptom
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({target.get()!!::duration}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({target.get()!!::cause}) { s -> Json.decodeFromString(s) })
            }
            return filler
        }
    }

}

data class Fever(override var session: UserSession? = null
) : IFrame, ISymptom {
    override var duration: Int? = null
    override var cause: String? = null
    var degree: Int? = null
    override fun annotations(path: String): List<Annotation> = when(path) {
        "duration" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("duration", "kotlin.Int", simpleTemplates({ "How long did it last?" }))
            )
        )
        "degree" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("degree", "kotlin.Int", simpleTemplates({ "What is your temperature?" }))
            )
        )
        "cause" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("cause", "kotlin.String", simpleTemplates({ "what is the cause for it?" }))
            )
        )
        else -> listOf()
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: Fever? = this@Fever
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({target.get()!!::duration}) { s -> Json.decodeFromString(s)})
                addWithPath(EntityFiller({target.get()!!::cause}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({target.get()!!::degree}) { s -> Json.decodeFromString(s) })
            }
            return filler
        }
    }
}



data class Headache(override var session: UserSession? = null
) : ISymptom, IFrame {
    override var duration: Int? = null
    override var cause: String? = null
    var part: String? = null

    override fun annotations(path: String): List<Annotation> = when(path) {
        "duration" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("duration", "kotlin.Int", simpleTemplates({ "How long did it last?" }))
            )
        )
        "cause" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("cause", "kotlin.String", simpleTemplates({ "What is the cause for it?" }))
            )
        )
        "part" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("part", "kotlin.String", simpleTemplates({ "which part of head?" }))
            )
        )
        else -> listOf()
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object: FillBuilder {
        var frame : Headache? = this@Headache
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({target.get()!!::duration}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({target.get()!!::cause}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({target.get()!!::part}) { s -> Json.decodeFromString(s) })
            }
            return filler
        }
    }
}


data class Person(override var session: UserSession? = null
) : ISingleton {

    var name: String? = null
    var age: Int? = null
    var height: Int? = null
    var weight: Int? = null

    override fun annotations(path: String): List<Annotation> = when(path) {
        "name" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("name", "kotlin.String", simpleTemplates({ "What is your name?" }))
            )
        )

        "age" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("age", "kotlin.Int", simpleTemplates({ "How old are you?" }))
            )
        )

        "height" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("height", "kotlin.Int", simpleTemplates({ "How tall are you?" }))
            )
        )

        else -> listOf()
    }

    override lateinit var filler: FrameFiller<*>


    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object: FillBuilder {
        var frame:Person? = this@Person
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({frame!!::name}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({frame!!::age}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({frame!!::height}) { s -> Json.decodeFromString<Int>(s) })
                addWithPath(EntityFiller({frame!!::weight}) { s -> Json.decodeFromString<Int>(s) })
            }
            return filler
        }
    }

}


data class MobileWithAdvances(@JsonInclude(JsonInclude.Include.NON_NULL) override var session: UserSession? = null
) : IFrame {
    var name: String? = null
    var cellphone: String? = null
    var id: Int? = null

    @JsonIgnore
    var recommendation: PagedSelectable<MobileWithAdvances> = PagedSelectable(
        session, {mobileService.search_cellphone(name).map { MobileWithAdvances.from(it) }},
        { MobileWithAdvances::class },
        {offers -> SlotOffer(offers, "cellphone", "kotlin.String",
                simpleTemplates(
                    Prompt {
                        with(session) {
                            """We have following ${offers.size} choices for cellphones : ${
                                offers.joinToString(", ") {
                                    "(${it.id}, ${it.cellphone})"
                                }
                            }."""
                        }
                }))
        },
            target = this, slot = "", hard = true)

    override fun annotations(path: String): List<Annotation> = when(path) {
        "id" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("id", "kotlin.Int", simpleTemplates({ "What is your id?" }))
            )
        )
        "cellphone" -> listOf(
            SlotPromptAnnotation(
                SlotRequest(
                    "cellphone",
                    "kotlin.String",
                    simpleTemplates({ "What is your cell number? (MobileWithAdvances)" })
                )
            ),
            ValueRecAnnotation({ recommendation }, false)
        )
        "name" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("name", "kotlin.String", simpleTemplates({ "What is your name (MobileWithAdvances)?" }))
            ),
            ValueCheckAnnotation({OldValueCheck(session,
                { mobileService.search_cellphone(name).isNotEmpty() },
                listOf(Pair(this, "name")),
                {
                    SlotNotifyFailure(
                        name,
                        "name",
                        "kotlin.String",
                        FailType.VC,
                        simpleTemplates(Prompt { "your name has not been attached to a cellphone, let's try it again." })
                    )
                }
            )})
        )
        else -> listOf()
    }

    @get:JsonIgnore
    val mobileService: IMobileService
        get() = session!!.getExtension<IMobileService>() as IMobileService

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object :  FillBuilder {
        var frame: MobileWithAdvances? = this@MobileWithAdvances

        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({frame!!::name}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({frame!!::cellphone}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({frame!!::id}) { s -> Json.decodeFromString<Int>(s) })
            }
            return filler
        }
    }

    companion object {
        val mappings = mapOf<String, Map<String, String>>(
            "io.opencui.test.MobileWithAdvancesForMapping" to mapOf<String, String>(
                "id" to "id",
                "nameMapping" to "name",
                "cellphoneMapping" to "cellphone"
            )
        )
        inline fun <reified S: IFrame> from(s: S): MobileWithAdvances {
            return Json.mappingConvert(s)
        }
    }
}

data class Mobile(override var session: UserSession? = null) : IFrame {
    var cellphone: String? = null
    var amount: Float? = null
    var id: Int? = null

    @get:JsonIgnore
    val mobileService: IMobileService
        get() = session!!.getExtension<IMobileService>() as IMobileService

    @JsonIgnore
    val recommendation: PagedSelectable<Mobile> = PagedSelectable(
        session,
        {mobileService.search_mobile(cellphone)},
        {Mobile::class},
        {offers -> SlotOffer(offers, "amount", "kotlin.Float",
                simpleTemplates(
                    Prompt {
                        with(session) {
                            """We have following ${offers.size} choices: ${
                                offers.joinToString(", ") {
                                    "(${it.id}, ${it.amount})"
                                }
                            }."""
                        }
                }))
        },
        target = this, slot = "")

    override fun annotations(path: String): List<Annotation> = when(path) {
        "id" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("id", "kotlin.String", simpleTemplates({ "What is your id?" }))
            )
        )
        "cellphone" -> listOf(
            SlotPromptAnnotation(
                SlotRequest("cellphone", "kotlin.String", simpleTemplates("What is your cell number?"))
            ),
            ValueCheckAnnotation({OldValueCheck(session,
                { mobileService.search_mobile(cellphone).isNotEmpty() },
                listOf(Pair(this, "cellphone")),
                {
                    SlotNotifyFailure(
                        cellphone,
                        "cellphone",
                        "kotlin.String",
                        FailType.VC,
                        simpleTemplates(Prompt { "your cellphone number is not correct, let's try it again." })
                    )
                }
            )}))
        "amount" -> listOf(
            SlotPromptAnnotation(
                listOf(
                    SlotRequest(
                        "amount",
                        "kotlin.Float",
                        simpleTemplates("What much do you want?")
                    )
                )
            ),
            ValueRecAnnotation({ recommendation }, false)
        )
        else -> listOf()
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
         var frame: Mobile? = this@Mobile
         override fun invoke(path: ParamPath): FrameFiller<*> {
             val filler = FrameFiller({ ::frame }, path)
             with(filler) {
                 addWithPath(EntityFiller({frame!!::cellphone}) { s -> Json.decodeFromString(s) })
                 addWithPath(EntityFiller({frame!!::amount}) { s -> Json.decodeFromString(s) })
                 addWithPath(EntityFiller({frame!!::id}) { s -> Json.decodeFromString(s) })
             }
             return filler
         }
     }
}

data class HotelSuggestionIntent(override var session: UserSession? = null) : IIntent {
    var city: String? = null
    var hotel: String? = null
    var result: MutableList<String>? = null

    @get:JsonIgnore
    val vacationService: IVacationService
        get() = session!!.getExtension<IVacationService>() as IVacationService

    override fun annotations(path: String): List<Annotation> = when(path) {
        "city" -> listOf(NeverAsk())
        "hotel" -> listOf(NeverAsk())
        "result" -> listOf(NeverAsk(), SlotInitAnnotation(DirectlyFillActionBySlot({ initResult() }, this, "result")))
        else -> listOf()
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: HotelSuggestionIntent? = this@HotelSuggestionIntent
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val tp = p as? KMutableProperty0<HotelSuggestionIntent?> ?: ::frame
            val filler = FrameFiller({ tp }, path)
            with(filler) {
                addWithPath(EntityFiller({tp.get()!!::city}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({tp.get()!!::hotel}) { s -> Json.decodeFromString(s) })
                addWithPath(MultiValueFiller(
                    { frame!!::result },
                    fun(p: KMutableProperty0<String?>): AEntityFiller { return EntityFiller({p}) { s -> Json.decodeFromString(s) } }))
            }
            return filler
        }
    }

    fun initResult(): List<String> {
        return if (hotel != null) listOf(hotel!!) else vacationService.searchHotelByCity(city)
    }
}

data class Hotel(override var session: UserSession? = null
) : IFrame {
    var city: String? = null
    var hotel: String? = null

    @get:JsonIgnore
    val vacationService: IVacationService
        get() = session!!.getExtension<IVacationService>() as IVacationService

    @JsonIgnore
    var pagedSelectable: String?.() -> PagedSelectable<String> = {PagedSelectable(
        session, JsonFrameBuilder("""{"@class": "io.opencui.test.HotelSuggestionIntent"}""", constructorParameters = listOf(session), slotAssignments = mapOf("city" to {city}, "hotel" to {this})),
        {String::class},
        {offers -> SlotOffer(offers, "hotel", "kotlin.String",
                simpleTemplates(
                    Prompt {
                    with(session) {
                        """We have following ${offers.size} choices: ${
                            offers.joinToString(", ") {
                                "(${it})"
                            }
                        }."""
                    }
                }))
        },
            pageSize = 2, target = this@Hotel, slot = "hotel", hard = true)}
    override fun annotations(path: String): List<Annotation> = when(path) {
        "city" -> listOf(
            SlotPromptAnnotation(listOf(SlotRequest("city", "kotlin.String", simpleTemplates("""Which city?""")))),
            ValueCheckAnnotation({OldValueCheck(session,
                { vacationService.searchHotelByCity(city).isNotEmpty() },
                listOf(Pair(this, "city")),
                {
                    SlotNotifyFailure(
                        city,
                        "city",
                        "kotlin.String",
                        FailType.VC,
                        simpleTemplates(Prompt { "No hotel available for the city you have chosen." })
                    )
                }
            )}))
        "hotel" -> listOf(
            SlotPromptAnnotation(listOf(SlotRequest("hotel", "kotlin.String", simpleTemplates("""Which hotel?""")))),
            TypedValueRecAnnotation<String>({ pagedSelectable(this) }, false)
        )
        else -> listOf()
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object: FillBuilder {
        var frame: Hotel? = this@Hotel

        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({frame!!::city}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({frame!!::hotel}) { s -> Json.decodeFromString(s) })
            }
            return filler
        }
    }

}

data class PreDiagnosis(override var session: UserSession? = null
) : IIntent {

    var method: PayMethod? = null

    @get:JsonIgnore
    val person: Person?
        get() = session?.getGlobal()
    var headaches: MutableList<Headache>? = null
    @JsonIgnore
    var symptoms: MutableList<ISymptom>? = null
    var indexes: MutableList<Int>? = null

    override fun annotations(path: String): List<Annotation> = when(path) {
        "headaches" -> listOf(
            SlotConditionalPromptAnnotation(
                listOf(
                    LazyAction {
                        if (headaches!!.isEmpty())
                            SlotRequest(
                                "headaches", "kotlin.collections.List<io.opencui.test.Headache>", simpleTemplates(
                                    Prompt { "What kind of headache do you have?" })
                            )
                        else
                            SlotRequestMore(
                                "headaches", "kotlin.collections.List<io.opencui.test.Headache>", simpleTemplates(
                                    Prompt { "What kind of headache do you still have?" })
                            )
                    })
            )
        )
        "symptoms" -> listOf(
            SlotConditionalPromptAnnotation(
                listOf(
                    LazyAction {
                        if (symptoms!!.isEmpty())
                            SlotRequest(
                                "symptoms", "kotlin.collections.List<io.opencui.test.ISymptom>", simpleTemplates(
                                    Prompt { "What symptom do you have?" })
                            )
                        else
                            SlotRequestMore(
                                "symptoms", "kotlin.collections.List<io.opencui.test.ISymptom>", simpleTemplates(
                                    Prompt { "What symptom do you still have?" })
                            )
                    })
            )
        )
        "indexes" -> listOf(
            SlotConditionalPromptAnnotation(
                listOf(
                    LazyAction {
                        if (indexes!!.isEmpty())
                            SlotRequest(
                                "indexes",
                                "kotlin.collections.List<kotlin.Int>",
                                simpleTemplates(Prompt { "What id do you have?" })
                            )
                        else
                            SlotRequestMore(
                                "indexes",
                                "kotlin.collections.List<kotlin.Int>",
                                simpleTemplates(Prompt { "What id do you still have?" })
                            )
                    })
            )
        )
        "indexes._item" -> listOf(
            SlotPromptAnnotation(
                listOf(
                    SlotRequest(
                        "indexes._item",
                        "kotlin.Int",
                        simpleTemplates("what is the id?")
                    )
                )
            ),
        )
        "method" -> listOf(
            SlotPromptAnnotation(
                listOf(
                    SlotRequest(
                        "method",
                        "io.opencui.test.PayMethod",
                        simpleTemplates("What pay method do you perfer?")
                    )
                )
            ),
        )
        else -> listOf()
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: PreDiagnosis? = this@PreDiagnosis
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)

            filler.addWithPath(EntityFiller<PayMethod>({frame!!::method}, { s: String? -> method?.origValue = s}) { s -> Json.decodeFromString(s) })

            filler.add(frame!!.session!!.getGlobalFiller<Person>()!!)

            val headachesFiller = MultiValueFiller(
                { frame!!::headaches },
                fun(p: KMutableProperty0<Headache?>): ICompositeFiller {
                    val builder = p.apply { set(Headache(frame!!.session)) }.get()!!.createBuilder()
                    return builder.invoke(path.join("headaches._item", p.get()))
                }
            )
            filler.addWithPath(headachesFiller)

            val mffiller = MultiValueFiller(
                { frame!!::symptoms },
                fun(p: KMutableProperty0<ISymptom?>): ICompositeFiller {
                    return InterfaceFiller({p}, createFrameGenerator(frame!!.session!!, "io.opencui.test.ISymptom")) }
            )
            filler.addWithPath(mffiller)

            val msfiller = MultiValueFiller(
                { frame!!::indexes },
                fun(p: KMutableProperty0<Int?>): AEntityFiller { return EntityFiller({p}) { s -> Json.decodeFromString(s) } })
            filler.addWithPath(msfiller)

            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            indexes!!.size > 1 -> PreDiagnosisAction(this)
            else -> PreDiagnosisListAction(this)
        }
    }
}


data class Hi(override var session: UserSession? = null
) : IIntent {
    @get:JsonIgnore
    val person: Person?
        get() = session?.getGlobal()

    @JsonIgnore
    var symptom: ISymptom? = null
    override fun annotations(path: String): List<Annotation> = when(path) {
        "symptom" -> listOf(
            SlotPromptAnnotation(
                listOf(
                    SlotRequest(
                        "symptom",
                        "io.opencui.test.ISymptom",
                        simpleTemplates("What symptom do you have?")
                    )
                )
            )
        )
        else -> listOf()
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: Hi? = this@Hi
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            filler.add(frame!!.session!!.getGlobalFiller<Person>()!!)
            filler.addWithPath(
                InterfaceFiller({ frame!!::symptom }, createFrameGenerator(frame!!.session!!, "io.opencui.test.ISymptom")))
            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            person!!.age!! <= 60 && symptom!!.duration!! > 1 -> HiAction_0(this)
            person!!.age!! > 60 -> HiAction_1(this)
            else -> null
        }
    }
}



data class Hello(override var session: UserSession? = null
) : IIntent {
    var mobile_with_adcances: MobileWithAdvances? = MobileWithAdvances(session)
    var mobile: Mobile? = Mobile(session)
    var payable: PayMethod? = null

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: Hello? = this@Hello
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)

            // Need to first create the frame, before we can access
            // component. We might be better off without using ?
            // In KMutableProperty.
            filler.add(frame!!.mobile_with_adcances!!.createBuilder().invoke(path.join( "mobile_with_advances", mobile_with_adcances)))
            filler.add(frame!!.mobile!!.createBuilder().invoke(path.join("mobile", mobile)))
            filler.addWithPath(EntityFiller({ frame!!::payable }, { s: String? -> payable?.origValue = s}) { s -> Json.decodeFromString(s) })
            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> HelloAction(this)
        }
    }
}



data class BookFlight(override var session: UserSession? = null) : IIntent {
    var origin: String? = null
    var destination: String? = null
    var depart_date: String? = null
    var return_date: String? = null
    var flight: String? = null
    var extra: String? = null

    val vacationService: IVacationService
        get() = session!!.getExtension<IVacationService>() as IVacationService

    @JsonIgnore
    var recommendation: PagedSelectable<String> = PagedSelectable(session, {vacationService.search_flight(origin, destination)}, {String::class},
            {offers -> SlotOffer(offers, "this", "io.opencui.test.PayMethod",
                    simpleTemplates(
                        Prompt {
                        with(session) {
                            """We have following ${offers.size} choices: ${
                                offers.joinToString(", ") {
                                    "(${it})"
                                }
                            }."""
                        }
                    }))
            },
            target = this, slot = "flight", hard = true)
    override fun annotations(path: String): List<Annotation> = when(path) {
        "depart_date" -> listOf<Annotation>(SlotPromptAnnotation(simpleTemplates(Prompt { """When will you depart?""" })))
        "return_date" -> listOf<Annotation>(SlotPromptAnnotation(simpleTemplates(Prompt { """When will you return?""" })))
        "origin" -> listOf<Annotation>(SlotPromptAnnotation(simpleTemplates(Prompt { """From where?""" })))
        "destination" -> listOf<Annotation>(
            SlotPromptAnnotation(simpleTemplates(Prompt { """-> where?""" })),
            ValueCheckAnnotation({OldValueCheck(session,
                { vacationService.search_flight(origin, destination).isNotEmpty() },
                listOf(Pair(this, "destination")),
                {
                    SlotNotifyFailure(
                        destination,
                        "destination",
                        "kotlin.String",
                        FailType.VC,
                        simpleTemplates(Prompt { "No flight available for your origin and destination." })
                    )
                }
            )}))
        "flight" -> listOf<Annotation>(
            SlotPromptAnnotation(simpleTemplates(Prompt { """Which flight?""" })),
            ValueRecAnnotation({ recommendation }, false)
        )
        "extra" -> listOf(NeverAsk())
        "this" -> listOf(ConfirmationAnnotation({ searchConfirmation("this") }))
        else -> listOf()
    }

    @JsonIgnore
    var confirmThis: Confirmation = Confirmation(session, this, "",
            prompts = { SlotInform(this, "this", "io.opencui.test.BookFlight", simpleTemplates(Prompt { """flight $flight is booked for you""" })) },
            true)

    override fun searchConfirmation(slot: String): IFrame? {
        return when (slot) {
            "this" -> confirmThis
            else -> null
        }
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: BookFlight? = this@BookFlight

        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            filler.addWithPath(EntityFiller({frame!!::origin}) { s -> Json.decodeFromString(s) })
            filler.addWithPath(EntityFiller({frame!!::destination}) { s -> Json.decodeFromString(s) })
            filler.addWithPath(EntityFiller({frame!!::depart_date}) { s -> Json.decodeFromString(s) })
            filler.addWithPath(EntityFiller({frame!!::return_date}) { s -> Json.decodeFromString(s) })
            filler.addWithPath(EntityFiller({frame!!::flight}) { s -> Json.decodeFromString(s) })
            filler.addWithPath(EntityFiller({frame!!::extra}) { s -> Json.decodeFromString(s) })
            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> BookFlightAction_0(this)
        }
    }
}



data class BookHotel(override var session: UserSession? = null
) : IIntent {
    var checkin_date: String? = null
    var checkout_date: String? = null
    var hotel: Hotel? = Hotel(session)
    var placeHolder: String? = null
    override fun annotations(path: String): List<Annotation> = when(path) {
        "checkin_date" -> listOf<Annotation>(SlotPromptAnnotation(simpleTemplates(Prompt { """When to checkin?""" })))
        "checkout_date" -> listOf<Annotation>(SlotPromptAnnotation(simpleTemplates(Prompt { """When to checkout?""" })))
        "placeHolder" -> listOf<Annotation>(SlotPromptAnnotation(simpleTemplates(Prompt { """ask placeholder""" })))
        "this" -> listOf(ConfirmationAnnotation({ searchConfirmation("this") }))
        else -> listOf()
    }

    @JsonIgnore
    var confirmThis: Confirmation = Confirmation(session, this, "",
            { SlotConfirm(this, "this", "io.opencui.test.BookHotel", simpleTemplates(Prompt { """Are u sure of the hotel booking? hotel ${hotel?.hotel}""" })) }
    )

    override fun searchConfirmation(slot: String): IFrame? {
        return when (slot) {
            "this" -> confirmThis
            else -> null
        }
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: BookHotel? = this@BookHotel
        override fun invoke(path: ParamPath): FrameFiller<BookHotel> {
            val filler = FrameFiller({ ::frame }, path)
            filler.addWithPath(EntityFiller({frame!!::checkin_date}) { s -> Json.decodeFromString(s) })
            filler.addWithPath(EntityFiller({frame!!::checkout_date}) { s -> Json.decodeFromString(s) })
            filler.add(frame!!.hotel!!.createBuilder().invoke(path.join("hotel", hotel)))
            filler.addWithPath(EntityFiller({frame!!::placeHolder}) { s -> Json.decodeFromString(s) })
            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> BookHotelAction_0(this)
        }
    }
}

data class CreateUnimportant(override var session: UserSession? = null) : IFrame {
    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: CreateUnimportant? = this@CreateUnimportant
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            return filler
        }
    }
}

data class HotelAddress(override var session: UserSession? = null
) : IIntent {
    var hotel: Hotel? = Hotel(session)

    var unimportant: String? = null

    var address: String? = null
    override fun annotations(path: String): List<Annotation> = when(path) {
        "unimportant" -> listOf(SlotPromptAnnotation(simpleTemplates(Prompt { """unimportant?""" })))
        "createUnimportant" -> listOf()
        "address" -> listOf(NeverAsk())
        else -> listOf()
    }

    val vacationService: IVacationService
        get() = session!!.getExtension<IVacationService>() as IVacationService

    fun createUnimportant(): String {
        return "generated_unimportant"
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: HotelAddress? = this@HotelAddress
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            filler.add(frame!!.hotel!!.createBuilder().invoke(path.join("hotel", hotel)))
            filler.addWithPath(EntityFiller({frame!!::unimportant}) { s -> Json.decodeFromString(s) })
            filler.addWithPath(EntityFiller({frame!!::address}) { s -> Json.decodeFromString(s) })
            return filler
        }
    }

    override fun searchStateUpdateByEvent(event: String): IFrameBuilder? {
        val createUnimportant = CreateUnimportant(session)
        return when (event) {
            "io.opencui.test.CreateUnimportant" -> intentBuilder(
                createUnimportant,
                listOf(UpdateRule({ with(createUnimportant) { true } },
                    FillActionBySlot({ with(createUnimportant) { createUnimportant() } }, this, "unimportant"))
                )
            )
            else -> null
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> HotelAddressAction_0(this)
        }
    }
}



data class FirstLevelQuestion(override var session: UserSession? = null) : IIntent {
    var need_hotel: Boolean? = null
    override fun annotations(path: String): List<Annotation> = when(path) {
        "need_hotel" -> listOf<Annotation>(SlotPromptAnnotation(simpleTemplates(Prompt { """Do you need to book hotel?""" })))
        else -> listOf()
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: FirstLevelQuestion? = this@FirstLevelQuestion
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            filler.addWithPath(EntityFiller({frame!!::need_hotel}) { s -> Json.decodeFromString(s) })
            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> IntentAction(JsonFrameBuilder("{\"@class\": \"io.opencui.test.BookHotel\"}", listOf(session)))
        }
    }
}


data class BookVacation(override var session: UserSession? = null
) : IIntent {
    var book_flight: BookFlight? = BookFlight(session)
    var book_hotel: BookHotel? = BookHotel(session)
    override fun annotations(path: String): List<Annotation> = when(path) {
        "book_hotel.checkin_date" -> listOf(
            SlotInitAnnotation(
                FillActionBySlot(
                    { book_flight?.depart_date },
                    book_hotel,
                    "checkin_date"
                )
            )
        )
        else -> listOf()
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: BookVacation? = this@BookVacation
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            filler.add(frame!!.book_flight!!.createBuilder().invoke(path.join("book_flight", book_flight)))
            filler.add(frame!!.book_hotel!!.createBuilder().invoke(path.join( "book_hotel", book_hotel)))
            return filler
        }
    }
}


data class CompositeWithIIntent(override var session: UserSession? = null
) : IIntent {
    var skill: IIntent? = null
    override fun annotations(path: String): List<Annotation> = when(path) {
        "skill" -> listOf<Annotation>(SlotPromptAnnotation(simpleTemplates(Prompt { """What do you want to do?""" })))
        else -> listOf()
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: CompositeWithIIntent? = this@CompositeWithIIntent
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            filler.addWithPath(
                    InterfaceFiller({ frame!!::skill }, createFrameGenerator(frame!!.session!!, "io.opencui.core.IIntent")))
            return filler
        }
    }
}


data class MoreBasics(override var session: UserSession? = null
) : IIntent {
    var int_condition: Int? = null
    var bool_condition: Boolean? = null
    var conditional_slot: String? = null
    var payMethod: PayMethod? = null
    var associateSlot: String? = null

    @JsonIgnore
    fun associateClosure(): String {
        return payMethod!!.concreteMethod()
    }

    override fun annotations(path: String): List<Annotation> = when(path) {
        "int_condition" -> listOf(SlotPromptAnnotation(simpleTemplates(Prompt { """int condition?""" })))
        "bool_condition" -> listOf(SlotPromptAnnotation(simpleTemplates(Prompt { """bool condition?""" })))
        "payMethod" -> listOf(SlotPromptAnnotation(simpleTemplates(Prompt { """payMethod?""" })))
        "associateSlot" -> listOf(
            SlotPromptAnnotation(simpleTemplates(Prompt { """associate slot?""" })),
            SlotInitAnnotation(FillActionBySlot({ associateClosure() }, this, "associateSlot"))
        )
        "conditional_slot" -> listOf(
            SlotPromptAnnotation(simpleTemplates(Prompt { """condition met and ask conditional slot""" })),
            ConditionalAsk(
                Condition { int_condition != null && int_condition!! > 3 && bool_condition == true })
        )
        else -> listOf()
    }

    override fun searchResponse(): Action? {
        return when {
            else -> MoreBasics_0(this)
        }
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: MoreBasics? = this@MoreBasics
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            filler.addWithPath(EntityFiller({frame!!::int_condition}) { s -> Json.decodeFromString<Int>(s) })
            filler.addWithPath(EntityFiller({frame!!::bool_condition}) { s -> Json.decodeFromString<Boolean>(s) })
            filler.addWithPath(EntityFiller({frame!!::conditional_slot}) { s -> Json.decodeFromString(s) })
            filler.addWithPath(EntityFiller({frame!!::payMethod}, {s: String? -> payMethod?.origValue = s}) { s -> Json.decodeFromString(s) })
            filler.addWithPath(EntityFiller({frame!!::associateSlot}) { s -> Json.decodeFromString(s) })
            return filler
        }
    }
}


data class IntentNeedConfirm(override var session: UserSession? = null
) : IIntent {
    var intVar: Int? = null
    var boolVar: Boolean? = null
    var stringVal: String? = null
    override fun annotations(path: String): List<Annotation> = when(path) {
        "intVar" -> listOf<Annotation>(SlotPromptAnnotation(simpleTemplates(Prompt { """intVar?""" })))
        "boolVar" -> listOf<Annotation>(
            SlotPromptAnnotation(simpleTemplates(Prompt { """boolVar?""" })),
            ConfirmationAnnotation({ searchConfirmation("boolVar") })
        )
        "stringVal" -> listOf<Annotation>(SlotPromptAnnotation(simpleTemplates(Prompt { """stringVal?""" })))
        "this" -> listOf(ConfirmationAnnotation { searchConfirmation("this") })
        else -> listOf()
    }

    @JsonIgnore
    var confirmThis: Confirmation = Confirmation(session, this, "",
            { SlotConfirm(this, "this", "io.opencui.test.IntentNeedConfirm", simpleTemplates(Prompt { """r u sure of the frame values $intVar $boolVar $stringVal""" })) }
    )
    @JsonIgnore
    var confirmboolVar: Confirmation = Confirmation(session, this, "boolVar",
            { SlotConfirm(boolVar, "boolVar", "kotlin.Boolean", simpleTemplates(Prompt { """r u sure of bool value $boolVar""" })) }
    )

    override fun searchConfirmation(slot: String): IFrame? {
        return when (slot) {
            "this" -> confirmThis
            "boolVar" -> confirmboolVar
            else -> null
        }
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame: IntentNeedConfirm? = this@IntentNeedConfirm
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            filler.addWithPath(EntityFiller({frame!!::intVar}) { s -> Json.decodeFromString<Int>(s) })
            filler.addWithPath(EntityFiller({frame!!::boolVar}) { s -> Json.decodeFromString<Boolean>(s) })
            filler.addWithPath(EntityFiller({frame!!::stringVal}) { s -> Json.decodeFromString(s) })

            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> IntentNeedConfirm_0(this)
        }
    }
}


data class WeakRecommendation(override var session: UserSession? = null
) : IIntent {
    @JsonIgnore
    var recommendation: PagedSelectable<WeakRecommendation> = PagedSelectable(session, {getSuggestions()}, {WeakRecommendation::class},
            {offers -> SlotOffer(offers, "this", "io.opencui.test.WeakRecommendation",
                    simpleTemplates(
                        Prompt {
                        with(session) {
                            """We have following ${offers.size} choices for intents : ${
                                offers.joinToString(", ") {
                                    "${it.slotForWeakRec}"
                                }
                            }."""
                        }
                    }))
            }, target = this, slot = "")
    @JsonIgnore
    val recPayMethod = PayMethod.createRecFrame(session!!, this, "payMethod")
    override fun annotations(path: String): List<Annotation> = when(path) {
        "slotForWeakRec" -> listOf(SlotPromptAnnotation(simpleTemplates(Prompt { "weak rec slot?" })))
        "payMethod" -> listOf(
            SlotPromptAnnotation(simpleTemplates(Prompt { "payMethod?" })),
            ValueRecAnnotation({ recPayMethod }, false)
        )

        "this" -> listOf(ValueRecAnnotation({ recommendation }, false))
        else -> listOf()
    }

    var condition: Boolean? = null
    var payMethod: PayMethod? = null
    var slotForWeakRec: String? = null

    @JsonIgnore
    fun getSuggestions(): List<WeakRecommendation> {
        return if (condition != true) {listOf(
            WeakRecommendation(session).apply {
                slotForWeakRec = "rec 1"
            },
            WeakRecommendation(session).apply {
                slotForWeakRec = "rec 2"
            }
        )} else {
            listOf(
                WeakRecommendation(session).apply {
                    slotForWeakRec = "rec 3"
                },
                WeakRecommendation(session).apply {
                    slotForWeakRec = "rec 4"
                }
            )
        }
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame:WeakRecommendation? = this@WeakRecommendation

        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({frame!!::condition}) { s -> Json.decodeFromString<Boolean>(s) })
                addWithPath(EntityFiller({frame!!::payMethod}, {s: String? -> payMethod?.origValue = s}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({target.get()!!::slotForWeakRec}) { s -> Json.decodeFromString(s) })
            }
            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> UserDefinedInform(this, simpleTemplates(Prompt { """weak rec value is ${slotForWeakRec}, payMethod is ${payMethod}""" }))
        }
    }
}

interface IContractFrame: IFrame {
    var contractId: String?
}


data class ContractBasedIntentA(override var session: UserSession? = null): IIntent, IContractFrame {
    override fun annotations(path: String): List<Annotation> = when(path) {
        "a" -> listOf(SlotPromptAnnotation(simpleTemplates(Prompt { "a?" })))
        "contractId" -> listOf(SlotPromptAnnotation(simpleTemplates(Prompt { "contractId?" })))
        else -> listOf()
    }

    var a: String? = null
    override var contractId: String? = null

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame:ContractBasedIntentA? = this@ContractBasedIntentA
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({frame!!::a}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({frame!!::contractId}) { s -> Json.decodeFromString(s) })
            }
            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> ContractBasedIntentA_0(this)
        }
    }
}

data class ContractBasedIntentA_0(
    val frame: ContractBasedIntentA) : UserDefinedInform<ContractBasedIntentA>(frame, simpleTemplates(Prompt {
    with(
        frame
    ) { """Hi, a=$a; contractId=$contractId""" }
}))


data class ContractBasedIntentB(override var session: UserSession? = null): IIntent, IContractFrame {
    override fun annotations(path: String): List<Annotation> = when(path) {
        "b" -> listOf(SlotPromptAnnotation(simpleTemplates(Prompt { "b?" })))
        "contractId" -> listOf(SlotPromptAnnotation(simpleTemplates(Prompt { "contractId?" })))
        else -> listOf()
    }

    var b: String? = null
    override var contractId: String? = null

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame:ContractBasedIntentB? = this@ContractBasedIntentB
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({frame!!::b}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({frame!!::contractId}) { s -> Json.decodeFromString(s) })
            }
            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> ContractBasedIntentB_0(this)
        }
    }
}

data class ContractBasedIntentB_0(
        val frame: ContractBasedIntentB
) : UserDefinedInform<ContractBasedIntentB>(frame, simpleTemplates(Prompt { with(frame) { """Hi, a=$b; contractId=$contractId""" } }))


data class RecoverTestIntent(override var session: UserSession? = null): IIntent {
    override fun annotations(path: String): List<Annotation> = when(path) {
        "aaa" -> listOf(
            SlotPromptAnnotation(simpleTemplates(Prompt { "aaa?" }))
        )
        "bbb" -> listOf(
            SlotPromptAnnotation(simpleTemplates(Prompt { throw Exception("exception") })),
            ConditionalAsk(Condition { aaa == "aaa" })
        )
        else -> listOf()
    }

    var aaa: String? = null
    var bbb: String? = null

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame:RecoverTestIntent? = this@RecoverTestIntent
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({frame!!::aaa}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({frame!!::bbb}) { s -> Json.decodeFromString(s) })
            }
            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> RecoverTestIntent_0(this)
        }
    }
}

data class RecoverTestIntent_0(
        val frame: RecoverTestIntent
) : UserDefinedInform<RecoverTestIntent>(frame, simpleTemplates(Prompt { with(frame) { """Hi, aaa=$aaa; bbb=$bbb""" } }))

data class AssociationTestFrame(override var session: UserSession? = null): IFrame {
    override fun annotations(path: String): List<Annotation> = when(path) {
        "aaa" -> listOf(SlotPromptAnnotation(simpleTemplates(Prompt { "frame aaa?" })))
        "bbb" -> listOf(SlotPromptAnnotation(simpleTemplates(Prompt { "frame bbb?" })))
        "this" -> listOf(SlotInitAnnotation(FillActionBySlot({ associationSource() }, this, "")))
        else -> listOf()
    }

    var aaa: String? = null
    var bbb: String? = null

    fun associationSource(): AssociationTestFrame {
        return AssociationTestFrame(session).apply {
            aaa = "aaa"
            bbb = "bbb"
        }
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame:AssociationTestFrame? = this@AssociationTestFrame
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({frame!!::aaa}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({frame!!::bbb}) { s -> Json.decodeFromString(s) })
            }
            return filler
        }
    }
}


data class AssociationTestIntent(override var session: UserSession? = null): IIntent {
    override fun annotations(path: String): List<Annotation> = when(path) {
        "aaa" -> listOf(
            SlotPromptAnnotation(simpleTemplates(Prompt { "intent aaa?" })),
            SlotInitAnnotation(FillActionBySlot({ associationFrameA?.associationSource()?.aaa }, this, "aaa"))
        )
        "bbb" -> listOf(SlotPromptAnnotation(simpleTemplates(Prompt { "intent bbb?" })))
        "associationFrameB" -> listOf(
            SlotInitAnnotation(
                FillActionBySlot(
                    { associationSource() },
                    this,
                    "associationFrameB"
                )
            )
        )
        else -> listOf()
    }

    var aaa: String? = null
    var bbb: String? = null
    var associationFrameA: AssociationTestFrame? = AssociationTestFrame(session)
    var associationFrameB: AssociationTestFrame? = AssociationTestFrame(session)

    fun associationSource(): AssociationTestFrame {
        return AssociationTestFrame(session).apply {
            aaa = "ccc"
            bbb = "ddd"
        }
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame:AssociationTestIntent? = this@AssociationTestIntent
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({frame!!::aaa}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({frame!!::bbb}) { s -> Json.decodeFromString(s) })
                filler.add(frame!!.associationFrameA!!.createBuilder().invoke(path.join("associationFrameA", associationFrameA)))
                filler.add(frame!!.associationFrameB!!.createBuilder().invoke(path.join( "associationFrameB", associationFrameB)))
            }
            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> AssociationTestIntent_0(this)
        }
    }
}

data class AssociationTestIntent_0(
        val frame: AssociationTestIntent
) : UserDefinedInform<AssociationTestIntent>(frame, simpleTemplates(Prompt { with(frame) { """Hi, aaa=$aaa; bbb=$bbb; Aaaa=${associationFrameA?.aaa}; Abbb=${associationFrameA?.bbb}; Baaa=${associationFrameB?.aaa}; Bbbb=${associationFrameB?.bbb}""" } }))

data class ValueRecInteractionFrame(override var session: UserSession? = null): IFrame {
    var b: Int? = null
    var c: Boolean? = null
    override fun annotations(path: String): List<Annotation> = when(path) {
        "b" -> listOf(
            SlotPromptAnnotation(
                simpleTemplates(Prompt { "b?" })
            )
        )
        "c" -> listOf(
            SlotPromptAnnotation(
                simpleTemplates(Prompt { "c?" })
            )
        )
        else -> listOf()
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame:ValueRecInteractionFrame? = this@ValueRecInteractionFrame

        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({frame!!::b}) { s -> Json.decodeFromString(s) })
                addWithPath(EntityFiller({frame!!::c}) { s -> Json.decodeFromString(s) })
            }
            return filler
        }
    }
}



data class ValueRecInteractionIntent(override var session: UserSession? = null): IIntent {
    var a: Int? = null
    var targetFrame: ValueRecInteractionFrame? = ValueRecInteractionFrame(session)

    fun recData(): List<ValueRecInteractionFrame> {
        val candidates = listOf(
            ValueRecInteractionFrame(session).apply {
                b = 1
                c = true
            },
            ValueRecInteractionFrame(session).apply {
                b = 2
                c = true
            },
            ValueRecInteractionFrame(session).apply {
                b = 3
                c = true
            }
        )
        return when (a) {
            0 -> listOf()
            1 -> candidates.subList(0, 1)
            else -> candidates
        }
    }

    fun checkData(): Boolean {
        return targetFrame?.b in setOf(null, 1, 2, 3) && targetFrame?.c in setOf(null, true)
    }

    @JsonIgnore
    var _rec_frame: PagedSelectable<ValueRecInteractionFrame> = PagedSelectable(
        session,  {recData()}, {ValueRecInteractionFrame::class},
            {offers -> SlotOffer(offers, "targetFrame", "io.opencui.test.ValueRecInteractionFrame",
                    simpleTemplates(
                        Prompt {
                        with(session) {
                            """We have following ${offers.size} choices: ${
                                offers.joinToString(
                                    ", "
                                ) { "(${it.b}; ${it.c})" }
                            }."""
                        }
                    }))
            },
        target = this, slot = "targetFrame", hard = false,
        zeroEntryActions = listOf(SlotOfferZepInform("targetFrame", "io.opencui.test.ValueRecInteractionFrame", simpleTemplates(
            Prompt { """no values for b and c while a=${a}""" }))),
        singleEntryPrompt = { SlotOfferSepInform(it, "targetFrame", "io.opencui.test.ValueRecInteractionFrame", simpleTemplates(
            Prompt { """b=${it.b}; c=${it.c}; contextB=${targetFrame?.b} are applied""" })) },
        implicit = true
    )

    override fun annotations(path: String): List<Annotation> = when(path) {
        "a" -> listOf(
            SlotPromptAnnotation(simpleTemplates(Prompt { "a?" }))
        )
        "targetFrame" -> listOf(
            ValueRecAnnotation({ _rec_frame }, false),
            ValueCheckAnnotation({OldValueCheck(session, { checkData() }, listOf(Pair(this, "targetFrame")),
                {
                    SlotNotifyFailure(
                        targetFrame,
                        "targetFrame",
                        "io.opencui.test.ValueRecInteractionFrame",
                        FailType.VC,
                        simpleTemplates(
                            Prompt { "b=${targetFrame?.b}; c=${targetFrame?.c}; value check failed" })
                    )
                }
            )}))
        else -> listOf()
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame:ValueRecInteractionIntent? = this@ValueRecInteractionIntent

        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                addWithPath(EntityFiller({frame!!::a}) { s -> Json.decodeFromString(s) })
                filler.add(targetFrame!!.createBuilder().invoke(path.join("targetFrame", targetFrame)))
            }
            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> ValueRecInteractionIntent_0(this)
        }
    }
}

data class ValueRecInteractionIntent_0(
    val frame: ValueRecInteractionIntent
) : UserDefinedInform<ValueRecInteractionIntent>(frame, simpleTemplates(Prompt { with(frame) { """Hi, a=$a; b=${targetFrame?.b}; c=${targetFrame?.c}""" } }))

data class CustomizedRecommendationIntent(override var session: UserSession? = null): IIntent {
    var recommendation: PagedSelectable<Hotel> = PagedSelectable(session,  {recData()}, {Hotel::class},
            {offers -> SlotOffer(offers, "hotel", "io.opencui.test.Hotel",
                    simpleTemplates(Prompt {
                        with(session) {
                            """We have following ${offers.size} choices: ${
                                offers.joinToString(", ") {
                                    "(${it.city}; ${it.hotel})"
                                }
                            }."""
                        }
                    }))
            },
            target = this, slot = "hotel")

    override fun annotations(path: String): List<Annotation> = when(path) {
        "hotel.city" -> listOf(ValueRecAnnotation({recommendation}, false))
        else -> listOf()
    }

    var hotel: Hotel? = Hotel(session)

    fun recData(): List<Hotel> {
        return listOf(
            Hotel(session).apply {
                city = "Wuhan"
                hotel = "Wuhan D Hotel"
            },
            Hotel(session).apply {
                city = "Shanghai"
                hotel = "Shanghai B Hotel"
            }
        )
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?) = object : FillBuilder {
        var frame:CustomizedRecommendationIntent? = this@CustomizedRecommendationIntent

        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            with(filler) {
                add(frame!!.hotel!!.createBuilder().invoke(path.join( "hotel", hotel)))
            }
            return filler
        }
    }

    override fun searchResponse(): Action? {
        return when {
            else -> CustomizedRecommendation_0(this)
        }
    }
}

data class CustomizedRecommendation_0(
    val frame: CustomizedRecommendationIntent
) : UserDefinedInform<CustomizedRecommendationIntent>(frame, simpleTemplates(Prompt { with(frame) { """Hi, city=${hotel?.city}; hotel=${hotel?.hotel}""" } }))

data class Greeting(
    override var session: UserSession? = null
) : IIntent {
    override fun searchResponse(): Action? = when {
        else -> UserDefinedInform(this, simpleTemplates(Prompt { """Good day!""" }))
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?): FillBuilder = object : FillBuilder {
        var frame: Greeting? = this@Greeting

        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            return filler
        }
    }
}


data class Goodbye(
    override var session: UserSession? = null
) : IIntent {
    override fun searchResponse(): Action? = when {
        else -> UserDefinedInform(this, simpleTemplates(Prompt { """Have a nice day! """ }))
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?): FillBuilder = object : FillBuilder {
        var frame: Goodbye? = this@Goodbye

        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            return filler
        }
    }
}


data class Main(
    override var session: UserSession? = null
) : IIntent {
    var Greeting: Greeting? = Greeting(session)

    @JsonIgnore
    var skills: MutableList<IIntent>? = null

    var Goodbye: Goodbye? = Goodbye(session)

    val searchIntentsService: IIntentSuggestionService
        get() = session!!.getExtension<IIntentSuggestionService>() as IIntentSuggestionService

    var recommendation: PagedSelectable<IIntent> = PagedSelectable(session, {searchIntentsService.searchIntents()}, {IIntent::class},
            {offers -> SlotOffer(offers, "skills", "kotlin.collections.List<io.opencui.core.IIntent>",
                    simpleTemplates(
                        Prompt {
                        with(session!!) {
                            """We have following ${offers.size} choices: ${
                                offers.joinToString(", ") {
                                    "(${it.typeName()})"
                                }
                            }."""
                        }
                    }))
            },
            target = this, slot = "skills")

    override fun annotations(path: String): List<Annotation> = when(path) {
        "skills" -> listOf(
            SlotConditionalPromptAnnotation{
                if (skills!!.isEmpty()) simpleTemplates(Prompt { """What can I do for you? (Main)""" }) else simpleTemplates(
                    Prompt { """What else can I do for you? (Main)""" })
            },
            ValueRecAnnotation({ recommendation }, false)
        )
        else -> listOf()
    }

    override fun searchResponse(): Action? = when {
        else -> null
    }

    override fun createBuilder(p: KMutableProperty0<out Any?>?): FillBuilder = object : FillBuilder {
        var frame: Main? = this@Main
        override fun invoke(path: ParamPath): FrameFiller<*> {
            val filler = FrameFiller({ ::frame }, path)
            filler.add(frame!!.Greeting!!.createBuilder().invoke(path.join("Greeting", Greeting)))
            filler.addWithPath(MultiValueFiller(
                { frame!!::skills },
                fun(p: KMutableProperty0<IIntent?>): ICompositeFiller {
                    return InterfaceFiller({p}, createFrameGenerator(frame!!.session!!, "io.opencui.core.IIntent"))}))
            filler.add(frame!!.Goodbye!!.createBuilder().invoke(path.join( "Goodbye", Goodbye)))
            return filler
        }
    }
}




