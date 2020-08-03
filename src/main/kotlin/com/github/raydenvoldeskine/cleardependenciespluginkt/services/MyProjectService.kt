package com.github.raydenvoldeskine.cleardependenciespluginkt.services

import com.intellij.openapi.project.Project
import com.github.raydenvoldeskine.cleardependenciespluginkt.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
