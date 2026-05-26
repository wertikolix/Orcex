package ru.wertik.orcex.font.stix2

import android.content.Context
import android.graphics.Typeface

public object StixTwoMath {
    public fun load(context: Context): Typeface =
        Typeface.createFromAsset(context.assets, "orcex/fonts/STIXTwoMath-Regular.ttf")
}
