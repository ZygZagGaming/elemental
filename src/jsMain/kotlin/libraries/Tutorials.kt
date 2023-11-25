package libraries

import core.Tutorial

object Tutorials: Library<Tutorial>() {
    val welcome = register(
        "welcome",
        Tutorial(
            listOf(
                TutorialPages.titleScreenPage,
                TutorialPages.elementsPage,
                TutorialPages.reactionsPage,
                TutorialPages.specialReactionsPage,
                TutorialPages.heatPage
            ),
            "Welcome"
        )
    )
    val clockStarted = register(
        "clockStarted",
        Tutorial(
            listOf(
                TutorialPages.clockStartedHeatPage
            ),
            "Heat Dissipation"
        )
    )
    val clickers = register(
        "clickers",
        Tutorial(
            listOf(
                TutorialPages.clickerIntroPage,
                TutorialPages.clickerTweakPage
            ),
            "Clickers"
        )
    )
    val superclickers = register(
        "superclickers",
        Tutorial(
            listOf(
                TutorialPages.superclickerPage
            ),
            "Visual Click Speed"
        )
    )
    val alteredReactions = register(
        "altered_reactions",
        Tutorial(
            listOf(
                TutorialPages.alteredReactionsPage
            ),
            "Altered Reactions"
        )
    )
    val deltaReactionRespec = register(
        "delta_reaction_respec",
        Tutorial(
            listOf(
                TutorialPages.deltaReactionRespecPage
            ),
            "Delta Reaction Respec"
        )
    )
    val fractionalElements = register(
        "fractional_elements",
        Tutorial(
            listOf(
                TutorialPages.fractionalElementsPage
            ),
            "Fractional Elements",
            unlockedByDefault = true
        )
    )
    val formulas = register(
        "secret_formulas",
        Tutorial(
            listOf(
                TutorialPages.formulaIntroPage,
                TutorialPages.heatDissipationFormulaPage,
                TutorialPages.specialReactionScalingPage
            ),
            "Formulas",
            unlockedByDefault = true
        )
    )
    val duality = register(
        "duality",
        Tutorial(
            listOf(
                TutorialPages.dualityWelcomePage,
                TutorialPages.dualityResetExplanationPage,
                TutorialPages.dualityMilestonePage,
                TutorialPages.deltaElementsPage
            ),
            "Duality",
        )
    )
    val softlocks = register(
        "softlocks",
        Tutorial(
            listOf(
                TutorialPages.softlockPage
            ),
            "Softlocks"
        )
    )
}