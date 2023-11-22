package libraries

import core.ImageTextTutorialPage
import core.ImageTitleTutorialPage
import core.TextTutorialPage
import core.TutorialPage

object TutorialPages: Library<TutorialPage>() {
    val titleScreenPage = register(
        "titleScreenPage",
        ImageTitleTutorialPage(
            image = "images/tutorial/title_screen.png",
            imageAlt = "goober",
            titleText = "Elemental",
            subTitleText = "Made by ZygZag"
        )
    )
    val elementsPage = register(
        "elementsPage",
        ImageTextTutorialPage(
            image = "images/tutorial/elements.png",
            imageAlt = "elements",
            headerText = "Elements",
            text = "These are your Elements. Most Elements have a bubble showing the Element's symbol and count. You can right-click on Element bubbles to show additional information like bounds and rates."
        )
    )
    val reactionsPage = register(
        "reactionsPage",
        ImageTextTutorialPage(
            image = "images/tutorial/normalreactions.png",
            imageAlt = "reactions",
            headerText = "Reactions",
            text = "These are your Normal Reactions. Each one shows the Elements they consume on the left of the arrow, and the Elements they produce on the right. Normal Reactions can be used as many times as you can afford."
        )
    )
    val specialReactionsPage = register(
        "specialReactionsPage",
        ImageTextTutorialPage(
            image = "images/tutorial/specialreactions.png",
            imageAlt = "special reactions",
            headerText = "Special Reactions",
            text = "These are your Special Reactions. They consume and produce Elements like Normal Reactions, but also have an alternative effect. Special Reactions' cost and effect usually increase each time they are used."
        )
    )
    val heatPage = register(
        "heatPage",
        ImageTextTutorialPage(
            image = "images/tutorial/heat.png",
            imageAlt = "heat",
            headerText = "Heat",
            text = "Heat (h) is an Element that is shown visually around the ϟ bubble. Unlike other elements, heat has a maximum of 10h. You cannot use reactions that would take you over the maximum."
        )
    )
    val clockStartedHeatPage = register(
        "clockStartedHeatPage",
        ImageTextTutorialPage(
            image = "images/tutorial/heat.png",
            imageAlt = "heat",
            headerText = "Heat Dissipation",
            text = "Heat will now dissipate over time. It dissipates faster the more you have."
        )
    )
    val clickerIntroPage = register(
        "clickerIntroPage",
        ImageTextTutorialPage(
            image = "images/tutorial/goober.png",
            imageAlt = "clickers",
            headerText = "Clickers",
            text = "Clickers can be dragged around and when their key is pressed, they will click on the button they're placed on. You can click on a spot in the clicker dock to summon its clicker."
        )
    )
    val clickerTweakPage = register(
        "clickerTweakPage",
        ImageTextTutorialPage(
            image = "images/tutorial/goober.png",
            imageAlt = "clickerTweaking",
            headerText = "Clicker Tweaking",
            text = "Each Clicker can be right-clicked to bring up a menu where you can change the key it listens to, and which mode it's on. Currently, you only have Manual and Disabled modes unlocked."
        )
    )
    val superclickerPage = register(
        "superclickerPage",
        ImageTextTutorialPage(
            image = "images/tutorial/goober.png",
            imageAlt = "superclicker",
            headerText = "Visual Click Speed",
            text = "Once a clicker's cps surpasses 7, it will visually show click speed equal to cps / (7^floor(log_7(cps))). The real click speed is unchanged and can be viewed in the right-click menu."
        )
    )
    val alteredReactionsPage = register(
        "alteredReactionsPage",
        ImageTextTutorialPage(
            image = "images/tutorial/goober.png",
            imageAlt = "altered reactions",
            headerText = "Altered Reactions",
            text = "Altered Reactions are, well, altered reactions. They replace a former reaction, and their name always ends in an asterisk (*)."
        )
    )
    val deltaReactionRespecPage = register(
        "deltaReactionRespecPage",
        ImageTextTutorialPage(
            image = "images/tutorial/goober.png",
            imageAlt = "delta reaction respec",
            headerText = "Delta Reaction Respec",
            text = "With the Duality Milestone Respect unlocked, you can check the \"Respec\" checkbox under the Duality button, and on your next Duality, all of your Duality Reactions will be refunded at no additional cost."
        )
    )
    val fractionalElementsPage = register(
        "fractional_elements_page",
        TextTutorialPage(
            headerText = "Fractional Elements",
            text = "Elements are always displayed as an integer (except for \"${Resources.heat.symbol}\"). However, " +
                    "behind the scenes, all elements are stored as a decimal value. This can cause some weirdness, " +
                    "but in most cases this will benefit you more than hurt you, i.e. getting more than expected from a reaction."
        )
    )

    val formulaIntroPage = register(
        "formula_intro_page",
        TextTutorialPage(
            headerText = "Formulas",
            text = "This \"tutorial\" is a compendium of all hidden formulas that might be of use. Of course, since the game " +
                    "is open-source, you could go looking for these formulas in the source code, but I've decided to publish " +
                    "most of them here for easy access."
        )
    )
    val heatDissipationFormulaPage = register(
        "heat_dissipation_formula",
        TextTutorialPage(
            headerText = "Heat Dissipation",
            text = "Every tick, heat will decrease by the heat value * time since last tick * 0.1. This gives a simple " +
                    "decreasing exponential, meaning it's almost impossible to reach exactly 0 heat."
        )
    )
    val specialReactionScalingPage = register(
        "special_reaction_scaling",
        TextTutorialPage(
            headerText = "Special Reaction Cost Scaling",
            text = "${SpecialReactions.clockwork.name} and ${SpecialReactions.overheat.name}'s costs scale linearly.<br /><br /> " +
                    "${SpecialReactions.heatSink.name}'s cost scales quadratically, and ${SpecialReactions.exponEntial.name}'s " +
                    "scales exponentially (base 2).<br /><br /> ${SpecialReactions.massiveClock.name}'s costs scale linearly for the first 5 " +
                    "purchases, and then scale quadratically.<br /><br /> ${SpecialReactions.heatingUp.name}'s costs stay the same for 3 " +
                    "purchases, scale linearly for the next 4, and then scale quadratically."
        )
    )
    val dualityWelcomePage = register(
        "duality_welcome",
        ImageTextTutorialPage(
            headerText = "Welcome to Duality!",
            text = "You just carried out a Duality for the first time. Well done!",
            image = "images/tutorial/goober.png",
            imageAlt = ""
        )
    )
    val dualityResetExplanationPage = register(
        "duality_reset",
        ImageTextTutorialPage(
            headerText = "Welcome to Duality!",
            text = "Duality is now always available after reaching 1000000${Symbols.catalyst}. Upon Duality, everything on the Elements " +
                    "page is reset. You will receive one ${Symbols.alpha} for each Element that was at its upper bound, and one ${Symbols.omega} for each " +
                    "Element at its lower bound.",
            image = "images/tutorial/goober.png",
            imageAlt = ""
        )
    )
    val dualityMilestonePage = register(
        "duality_milestones",
        ImageTextTutorialPage(
            headerText = "Duality Milestones",
            text = "These are Duality Milestones. Each one requires a certain quantity of ${Symbols.alpha} and/or ${Symbols.omega} to activate, " +
                    "but ${Symbols.alpha} and ${Symbols.omega} are not consumed.",
            image = "images/tutorial/goober.png",
            imageAlt = ""
        )
    )
    val deltaElementsPage = register(
        "delta_elements",
        ImageTextTutorialPage(
            headerText = "Delta Elements",
            text = "These are Delta Elements. Delta Elements are rewarded for reaching a new highest rate of gain for any basic Element. They can be spent " +
                    "on Delta Reactions.",
            image = "images/tutorial/goober.png",
            imageAlt = ""
        )
    )
}