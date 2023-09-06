package libraries

import core.AlterableReaction
import core.Reaction
import core.elementStackOf

object NormalReactions: Library<Reaction>() {
    val aToB = register("a_to_b",
        Reaction(
            "A to B",
            elementStackOf(
                Elements.catalyst to 1.0,
                Elements.a to 1.0
            ),
            elementStackOf(
                Elements.b to 3.0
            )
        )
    )
    val bBackToA = register("b_back_to_a",
        AlterableReaction(
            "B back to A",
            { alterations ->
                when (alterations) {
                    0 -> elementStackOf(
                        Elements.b to 1.0
                    )
                    1 -> elementStackOf(
                        Elements.b to 2.0
                    )
                    else -> elementStackOf()
                }
            },
            { alterations ->
                when (alterations) {
                    0 -> elementStackOf(
                        Elements.catalyst to 3.0,
                        Elements.a to 2.0,
                        Elements.heat to 0.5
                    )
                    1 -> elementStackOf(
                        Elements.catalyst to 10.0,
                        Elements.a to 4.0,
                        Elements.heat to 1.0
                    )
                    else -> elementStackOf()
                }
            }
        )
    )
    val abcs = register("abcs",
        Reaction(
            "ABCs",
            elementStackOf(
                Elements.catalyst to 2.0,
                Elements.b to 29.0,
            ),
            elementStackOf(
                Elements.c to 1.0,
                Elements.heat to 3.5
            )
        )
    )
    val cminglyOp = register("cmingly_op",
        Reaction(
            "Cmingly OP",
            elementStackOf(
                Elements.heat to 8.0,
                Elements.c to 1.0,
            ),
            elementStackOf(
                Elements.a to 10.0,
                Elements.b to 1.0
            )
        )
    )
//    val cataClysm = register("cataclysm",
//        core.Reaction(
//            "CataClysm",
//            core.elementStackOf(
//                libraries.Elements.c to 4.0,
//            ),
//            core.elementStackOf(
//                libraries.Elements.catalyst to 40.0,
//                libraries.Elements.heat to 9.0
//            )
//        )
//    )
//    val dscent = register("dscent",
//        core.Reaction(
//            "Dscent",
//            core.elementStackOf(
//                libraries.Elements.a to 120.0,
//            ),
//            core.elementStackOf(
//                libraries.Elements.d to 2.0
//            )
//        )
//    )
    val over900 = register("over900",
        Reaction(
            "Over 900",
            elementStackOf(
                Elements.a to 901.0,
            ),
            elementStackOf(
                Elements.d to 1.0
            )
        )
    )
    val exotherm = register("exotherm",
        Reaction(
            "Exotherm",
            elementStackOf(
                Elements.catalyst to 10000.0,
                Elements.c to 120.0
            ),
            elementStackOf(
                Elements.e to 1.0,
                Elements.heat to 50.0
            )
        )
    )
}