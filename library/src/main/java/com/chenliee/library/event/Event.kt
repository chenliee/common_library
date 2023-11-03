package com.chenliee.library.event

import org.greenrobot.eventbus.EventBus

/**
 *@Author：chenliee
 *@Date：2023/11/2 16:52
 *Describe:
 */
class Event {
    companion object {
        val eventBus: EventBus = EventBus.getDefault()
    }

    class ReLoginEvent
}