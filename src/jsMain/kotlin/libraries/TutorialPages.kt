package libraries

import core.ImageTextTutorialPage
import core.ImageTitleTutorialPage
import core.TutorialPage

object TutorialPages: Library<TutorialPage>() {
    val titleScreenPage = register(
        "titleScreenPage",
        ImageTitleTutorialPage(
            image = "images/goober.png",
            imageAlt = "goober",
            titleText = "Elemental",
            subTitleText = "Made by ZygZag"
        )
    )
    val elementsPage = register(
        "elementsPage",
        ImageTextTutorialPage(
            image = "images/elements.png",
            imageAlt = "elements",
            headerText = "Elements",
            text = "These are your Elements. Most Elements have a bubble showing the Element's symbol and count. You can right-click on Element bubbles to show additional information like bounds and rates."
        )
    )
    val reactionsPage = register(
        "reactionsPage",
        ImageTextTutorialPage(
            image = "images/normalreactions.png",
            imageAlt = "reactions",
            headerText = "Reactions",
            text = "These are your Normal Reactions. Each one shows the Elements they consume on the left of the arrow, and the Elements they produce on the right. Normal Reactions can be used as many times as you can afford."
        )
    )
    val specialReactionsPage = register(
        "specialReactionsPage",
        ImageTextTutorialPage(
            image = "images/specialreactions.png",
            imageAlt = "special reactions",
            headerText = "Special Reactions",
            text = "These are your Special Reactions. They consume and produce Elements like Normal Reactions, but also have an alternative effect. Special Reactions' cost and effect usually increase each time they are used."
        )
    )
    val heatPage = register(
        "heatPage",
        ImageTextTutorialPage(
            image = "images/heat.png",
            imageAlt = "heat",
            headerText = "Heat",
            text = "Heat (h) is an Element that is shown visually around the ϟ bubble. Unlike other elements, heat has a maximum of 10h. You cannot use reactions that would take you over the maximum."
        )
    )
    val clockStartedHeatPage = register(
        "clockStartedHeatPage",
        ImageTextTutorialPage(
            image = "images/heat.png",
            imageAlt = "heat",
            headerText = "Heat Dissipation",
            text = "Heat will now dissipate over time. It dissipates faster the more you have."
        )
    )
    val clickerIntroPage = register(
        "clickerIntroPage",
        ImageTextTutorialPage(
            image = "images/goober.png",
            imageAlt = "clickers",
            headerText = "Clickers",
            text = "Clickers can be dragged around and when their key is pressed, they will click on the button they're placed on. You can click on a spot in the clicker dock to summon its clicker."
        )
    )
    val clickerTweakPage = register(
        "clickerTweakPage",
        ImageTextTutorialPage(
            image = "images/goober.png",
            imageAlt = "clickerTweaking",
            headerText = "Clicker Tweaking",
            text = "Each Clicker can be right-clicked to bring up a menu where you can change the key it listens to, and which mode it's on. Currently, you only have Manual and Disabled modes unlocked."
        )
    )
    val superclickerPage = register(
        "superclickerPage",
        ImageTextTutorialPage(
            image = "images/goober.png",
            imageAlt = "superclicker",
            headerText = "Visual Click Speed",
            text = "Once a clicker's cps surpasses 7, it will visually show click speed equal to cps / (7^floor(log_7(cps))). The real click speed is unchanged and can be viewed in the right-click menu."
        )
    )
    val alteredReactionsPage = register(
        "alteredReactionsPage",
        ImageTextTutorialPage(
            image = "images/goober.png",
            imageAlt = "altered reactions",
            headerText = "Altered Reactions",
            text = "Altered Reactions are, well, altered reactions. They replace a former reaction, and their name always ends in an asterisk (*)."
        )
    )
    val deltaReactionRespecPage = register(
        "deltaReactionRespecPage",
        ImageTextTutorialPage(
            image = "images/goober.png",
            imageAlt = "delta reaction respec",
            headerText = "Delta Reaction Respec",
            text = "With the Duality Milestone Respect unlocked, you can check the \"Respec\" checkbox under the Duality button, and on your next Duality, all of your Duality Reactions will be refunded at no additional cost."
        )
    )
}