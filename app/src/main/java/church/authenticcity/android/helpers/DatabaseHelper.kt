package church.authenticcity.android.helpers

import church.authenticcity.android.AuthenticApplication
import church.authenticcity.android.classes.AuthenticAppearance
import church.authenticcity.android.classes.AuthenticEvent
import church.authenticcity.android.classes.AuthenticTab
import com.google.firebase.database.*

class DatabaseHelper {
    companion object {
        private val rootReference: DatabaseReference
                get() = if (AuthenticApplication.useDevelopmentDatabase) FirebaseDatabase.getInstance().reference.child("dev") else FirebaseDatabase.getInstance().reference

        private fun observeSingleEvent(reference: DatabaseReference, completion: (DatabaseError?, DataSnapshot?) -> Unit) {
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    completion(p0, null)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    completion(null, p0)
                }
            })
        }

        private fun setKeepSynced(value: Boolean, reference: DatabaseReference): DatabaseReference {
            reference.keepSynced(value)
            return reference
        }

        fun loadAppearance(completion: (AuthenticAppearance) -> Unit) {
            observeSingleEvent(setKeepSynced(true, rootReference.child("appearance"))) { e, snap ->
                completion(if (e == null) AuthenticAppearance(snap!!.value as HashMap<String, Any>) else AuthenticAppearance.default)
            }
        }

        fun loadAllTabs(keepSynced: Boolean, completion: (DatabaseError?, List<AuthenticTab>?) -> Unit) {
            observeSingleEvent(setKeepSynced(keepSynced, rootReference.child("tabs"))) { e, snap ->
                if (e == null)
                    completion(null, snap!!.children.mapNotNull { Utils.Constructors.constructTab(it.value) })
                else
                    completion(e, null)
            }
        }

        fun loadTab(id: String, keepSynced: Boolean, completion: (DatabaseError?, AuthenticTab?) -> Unit) {
            observeSingleEvent(setKeepSynced(keepSynced, rootReference.child("tabs").child(id))) { e, snap ->
                if (e == null)
                    completion(null, Utils.Constructors.constructTab(snap!!.value))
                else completion(e, null)
            }
        }

        fun loadAllEvents(keepSynced: Boolean, completion: (DatabaseError?, List<AuthenticEvent>?) -> Unit) {
            observeSingleEvent(setKeepSynced(keepSynced, rootReference.child("events"))) { e, snap ->
                if (e == null)
                    completion(null, snap!!.children.mapNotNull { Utils.Constructors.constructEvent(it.value) })
                else
                    completion(e, null)
            }
        }

        fun loadEvent(id: String, keepSynced: Boolean, completion: (DatabaseError?, AuthenticEvent?) -> Unit) {
            observeSingleEvent(setKeepSynced(keepSynced, rootReference.child("events").child(id))) { e, snap ->
                if (e == null)
                    completion(null, Utils.Constructors.constructEvent(snap!!.value))
                else completion(e, null)
            }
        }
     }
}