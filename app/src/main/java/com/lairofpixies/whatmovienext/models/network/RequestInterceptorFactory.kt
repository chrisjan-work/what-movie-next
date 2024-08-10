/*
 * This file is part of What Movie Next.
 *
 * Copyright (C) 2024 Christiaan Janssen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.lairofpixies.whatmovienext.models.network

import com.lairofpixies.whatmovienext.BuildConfig
import okhttp3.Interceptor
import okhttp3.Request

object RequestInterceptorFactory {
    private fun interceptor(injector: Request.() -> Request): Interceptor =
        Interceptor { chain -> chain.proceed(chain.request().injector()) }

    fun tmdbInterceptor(): Interceptor =
        interceptor {
            newBuilder()
                .header("Authorization", "Bearer ${BuildConfig.tmdbtoken}")
                .header("User-Agent", USER_AGENT)
                .build()
        }

    fun omdbInterceptor(): Interceptor =
        interceptor {
            newBuilder()
                .url(
                    url
                        .newBuilder()
                        .addQueryParameter("apikey", BuildConfig.omdbkey)
                        .build(),
                ).header("User-Agent", USER_AGENT)
                .build()
        }

    fun wikidataInterceptor(): Interceptor =
        interceptor {
            newBuilder()
                .header("User-Agent", USER_AGENT)
                .build()
        }

    private const val USER_AGENT = "WhatMovieNext/1.0 (Android; ${BuildConfig.VERSION_NAME})"
}
