package church.authenticcity.android.classes

import church.authenticcity.android.helpers.getAs

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 6/18/2018 at 7:18 PM.
 * Licensed under the MIT License.
 */
class ImageResource(val imageName: String, val width: Int, val height: Int) {
    constructor(map: HashMap<String, Any>) : this(map.getAs("name"), map.getAs("width"), map.getAs("height"))
    constructor() : this("unknown.png", 720, 1080)
}