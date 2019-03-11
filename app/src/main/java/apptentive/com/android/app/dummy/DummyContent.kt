package apptentive.com.android.app.dummy

import apptentive.com.android.app.R

object DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    val BEVERAGES = listOf(
        Beverage("1739787", R.drawable.ic_coffee_01, "Iced Caramel Cloud Macchiato"),
        Beverage("4686989", R.drawable.ic_coffee_02, "Iced Cinnamon Cloud Macchiato"),
        Beverage("7501926", R.drawable.ic_coffee_03, "Iced Matcha Green Tea Latte"),
        Beverage("2724579", R.drawable.ic_coffee_04, "Cold Brew with Cascara Cold Foam"),
        Beverage("6402270", R.drawable.ic_coffee_05, "Nitro Cold Brew"),
        Beverage("6810919", R.drawable.ic_coffee_06, "Matcha Green Tea Crème Frappuccino®"),
        Beverage("5951855", R.drawable.ic_coffee_07, "Starbucks® Blonde Flat White"),
        Beverage("2225867", R.drawable.ic_coffee_08, "Mango Dragonfruit Starbucks Refreshers® Beverage"),
        Beverage("1598848", R.drawable.ic_coffee_09, "Iced Passion Tango™ Tea Lemonade")
    )

    data class Beverage(val id: String, val imageId: Int, val title: String) {
        override fun toString(): String = title
    }
}
