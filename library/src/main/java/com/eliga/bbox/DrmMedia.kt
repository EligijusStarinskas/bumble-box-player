package com.eliga.bbox

/**
 * Created by eliga on 21/02/2017.
 */

interface DrmMedia {

    val type: String

    val licenseUrl: String

    val keyRequestPropertiesArray: Array<String>
}
