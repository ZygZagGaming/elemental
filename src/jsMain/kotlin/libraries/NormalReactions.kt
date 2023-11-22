package libraries

import core.AlterableReaction
import core.Reaction
import core.elementStackOf

object NormalReactions: Library<Reaction>() {
    val aToB = register("a_to_b",
        Reaction(
            "A to B",
            elementStackOf(
                Resources.catalyst to 1.0,
                Resources.a to 1.0
            ),
            elementStackOf(
                Resources.b to 3.0
            )
        )
    )
    val bBackToA = register("b_back_to_a",
        AlterableReaction(
            "B back to A",
            { alterations ->
                when (alterations) {
                    0 -> elementStackOf(
                        Resources.b to 1.0
                    )
                    1 -> elementStackOf(
                        Resources.b to 1.0,
                        Resources.heat to 0.1
                    )
                    else -> elementStackOf()
                }
            },
            { alterations ->
                when (alterations) {
                    0 -> elementStackOf(
                        Resources.catalyst to 3.0,
                        Resources.a to 2.0,
                        Resources.heat to 0.5
                    )
                    1 -> elementStackOf(
                        Resources.catalyst to 8.0,
                        Resources.a to 4.0
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
                Resources.catalyst to 2.0,
                Resources.b to 29.0,
            ),
            elementStackOf(
                Resources.c to 1.0,
                Resources.heat to 3.5
            )
        )
    )
    val cminglyOp = register("cmingly_op",
        Reaction(
            "C-mingly OP",
            elementStackOf(
                Resources.heat to 8.0,
                Resources.c to 1.0,
            ),
            elementStackOf(
                Resources.a to 10.0,
                Resources.b to 1.0
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
                Resources.a to 901.0,
            ),
            elementStackOf(
                Resources.d to 1.0
            )
        )
    )
    val exotherm = register("exotherm",
        Reaction(
            "Exotherm",
            elementStackOf(
                Resources.catalyst to 10000.0,
                Resources.c to 120.0
            ),
            elementStackOf(
                Resources.e to 1.0,
                Resources.heat to 50.0
            )
        )
    )
}