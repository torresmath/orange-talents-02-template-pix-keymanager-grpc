package br.com.torresmath.key.manager

import io.micronaut.runtime.Micronaut.build

fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.torresmath.key.manager")
		.start()
}

