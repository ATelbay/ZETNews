package com.smqpro.zetnews.util

import java.util.*

class Constants {

    companion object {
        /** The Guardian's Base URL */
        const val BASE_URL = "https://content.guardianapis.com";

        /** API_KEY */
        const val API_KEY = "8c3401b3-3022-4849-8781-826bb87826d1"

        /** Parameters  */
        const val QUERY_PARAM = "q"
        const val ORDER_BY_PARAM = "order-by"
        const val PAGE_SIZE_PARAM = "page-size"
        const val ORDER_DATE_PARAM = "order-date"
        const val PAGE_PARAM = "page"
        const val FROM_DATE_PARAM = "from-date"
        const val SHOW_FIELDS_PARAM = "show-fields"
        const val FORMAT_PARAM = "format"
        const val SHOW_TAGS_PARAM = "show-tags"
        const val API_KEY_PARAM = "api-key"
        const val SECTION_PARAM = "section"

        /**  */
        const val SEARCH_DELAY = 800L
    }

    enum class ORDER {
        OLDEST,
        NEWEST,
        RELEVANCE;

        override fun toString(): String {
            return name.toLowerCase(Locale.ROOT)
        }}

}