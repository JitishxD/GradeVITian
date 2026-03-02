package me.jitish.gradevitian.ui.navigation

import me.jitish.gradevitian.domain.model.CgpaRecord
import me.jitish.gradevitian.domain.model.GpaRecord
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared holder for passing record data between History → Calculator screens.
 * Consumed once on read (set to null after reading).
 */
@Singleton
class PendingRecordHolder @Inject constructor() {
    var pendingGpaRecord: GpaRecord? = null
    var pendingCgpaRecord: CgpaRecord? = null
}

