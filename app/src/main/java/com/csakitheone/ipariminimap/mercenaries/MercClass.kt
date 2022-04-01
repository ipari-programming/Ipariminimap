package com.csakitheone.ipariminimap.mercenaries

class MercClass(
    val id: String,
    val name: String,
    val baseAttack: Int,
    val baseHealth: Int,
    val levelUpAttack: Int = 1,
    val levelUpHealth: Int = 2,
) {
    var abilities = mutableListOf<Ability>()

    init {
        allClasses.add(this)
    }

    fun setAbilities(vararg abilities: Ability): MercClass {
        this.abilities = abilities.toMutableList()
        this.abilities.map { it.prepareForGame(1) }
        return this
    }

    companion object {
        private var allClasses = mutableListOf<MercClass>()

        fun getAll(): List<MercClass> = allClasses

        val classCompanionAnimal = MercClass("classCompanionAnimal", "Háziállat", 1, 3, 1, 1)
            .setAbilities(
                Ability("Harapj!", 2)
            )
        val classBird = MercClass("classBird", "Madár", 1, 2, 1, 1)
            .setAbilities(
                Ability("Célozz a szemére!", 5)
            )
        val classRobot = MercClass("classRobot", "Robot", 1, 4, 1, 1)
            .setAbilities(
                Ability("attack(random)", 5, "Megtámad egy random ellenséget").attack(Ability.Target.Enemy.RANDOM),
                Ability("while(isAlive)", 8, "Addig támad random ellenségeket, amíg meg nem hal").attack(Ability.Target.Enemy.RANDOM).loop()
            )

        val classStudent = MercClass("classStudent", "Diák", 2, 15)
            .setAbilities(
                Ability("Ütés", 5).attack(Ability.Target.Enemy.RANDOM)
            )

        val classVegyesz = MercClass("classVegyesz", "Vegyész", 1, 20) // 21 caster
            .setAbilities(
                Ability("Marás", 5, "Egy maró vegyületet dob egy ellenségre", mapOf(Ability.varAmount() to 3))
                    .attack(Ability.Target.CHOICE, Ability.varAmount()),
                Ability("Gyengítés", 4, "Csökkenti egy ellenség sebzését", mapOf(Ability.varAmount() to 2))
                    .weaken(Ability.Target.CHOICE, Ability.varAmount()),
                Ability("Kábítás", 3, "Elkábít egy ellenséget a kör végéig").stun(Ability.Target.CHOICE)
            )
        val classKornyezetes = MercClass("classKornyezetes", "Környezetes", 2, 15) // 17 summoner
            .setAbilities(
                Ability("Támadj!", 5, "Megüt egy választott ellenséget és idéz egy háziállatot")
                    .attack(Ability.Target.CHOICE)
                    .summon(classCompanionAnimal),
                Ability("Célozz a szemére!", 5, "Idéz egy madarat, ami azonnal támad")
                    .summon(classBird)
            )
        val classInfos = MercClass("classInfos", "Infós", 1, 20) // 21 caster
            .setAbilities(
                Ability("build()", 5, "+1 sebzést ad magának és támad")
                    .strengthen(Ability.Target.SELF, "1")
                    .attack(Ability.Target.CHOICE),
                Ability("send()", 3, "Egy random ellenség sebzését átadja egy barátnak")
                    .weaken(Ability.Target.Enemy.RANDOM, "1")
                    .strengthen(Ability.Target.CHOICE, "1")
            )
        val classGepesz = MercClass("classGepesz", "Gépész", 4, 20, 2, 3) // 24 tank
            .setAbilities(
                Ability( "Roham", 2, "Egy gyors támadás")
                    .attack(Ability.Target.CHOICE),
                Ability( "Dobd az asztalt!", 6, "Minden ellenséget eltalál", mapOf(Ability.varAmount() to 1))
                    .attack(Ability.Target.Enemy.ALL, Ability.varAmount())
            )
        val classMechas = MercClass("classMechas", "Mechás", 2, 15) // 17 summoner
            .setAbilities(
                Ability("Engineer gaming", 5, "Épít egy robotot, ami megtámad egy random ellenséget")
                    .summon(classRobot, Ability.SUMMON_TAG_FORCA_AUTO_ATTACK, Ability.SUMMON_LEARN_ABILITY_1),
                Ability("Végtelen ciklus", 6, "Épít egy robotot, ami addig sebzi az ellenségeket, amíg bele nem hal")
                    .summon(classRobot, Ability.SUMMON_TAG_FORCA_AUTO_ATTACK, Ability.SUMMON_LEARN_ABILITY_2)
            )
        val classMuanyagos = MercClass("classMuanyagos", "Műanyagos", 3, 18) // 21 caster
            .setAbilities(
                Ability("Gyenge marás", 5, "Egy gyenge maró vegyületet dob egy ellenségre", mapOf(Ability.varAmount() to 2))
                    .attack(Ability.Target.CHOICE, Ability.varAmount()),
                Ability("Gyengítés", 4, "Csökkenti az ellenség sebzését", mapOf(Ability.varAmount() to 1))
                    .weaken(Ability.Target.CHOICE, Ability.varAmount())
            )
        val classGondozo = MercClass("classGondozo", "Gondozó", 1, 15) // 16 healer
            .setAbilities(
                Ability("Bátorítás", 3, "Gyógyít egy barátságos karaktert", mapOf(Ability.varAmount() to 4))
                    .heal(Ability.Target.CHOICE, Ability.varAmount()),
                Ability("Csoportos ölelés", 8, "Minden barátságos karaktert gyógyíít", mapOf(Ability.varAmount() to 2))
                    .heal(Ability.Target.Friend.ALL, Ability.varAmount()),
                Ability("Jól vagyok!", 5, "Megüt egy ellenséget és gyógyít magán", mapOf(Ability.varAmount() to 5))
                    .attack(Ability.Target.CHOICE)
                    .heal(Ability.Target.SELF, Ability.varAmount())
            )

        fun fromMajor(major: String): MercClass {
            return when (major.toLowerCase()) {
                "a" -> classVegyesz
                "b" -> classKornyezetes
                "c" -> classInfos
                "d" -> classGepesz
                "e" -> classMechas
                "f" -> classMuanyagos
                "g" -> classGondozo
                "ny" -> classGondozo
                else -> classStudent
            }
        }

    }
}