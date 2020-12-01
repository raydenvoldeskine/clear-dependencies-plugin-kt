package com.github.raydenvoldeskine.cleardependenciespluginkt.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.github.raydenvoldeskine.cleardependenciespluginkt.services.MyProjectService

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        //project.getService(MyProjectService::class.java)
    }
}
