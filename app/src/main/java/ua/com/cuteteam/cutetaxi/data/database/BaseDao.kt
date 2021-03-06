package ua.com.cuteteam.cutetaxi.data.database

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ua.com.cuteteam.cutetaxi.data.User
import ua.com.cuteteam.cutetaxi.data.entities.Order
import ua.com.cuteteam.cutetaxi.extentions.exists
import ua.com.cuteteam.cutetaxi.extentions.getValue

abstract class BaseDao(
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    database: FirebaseDatabase = Firebase.database
) {

    protected val authUser = auth.currentUser!!
    protected val rootRef = database.reference.root
    protected val ordersRef = database.reference.root.child(DbEntries.Orders.TABLE)
    protected abstract val usersRef: DatabaseReference

    protected val eventListeners = mutableMapOf<DatabaseReference, Any>()

    abstract suspend fun getUser(uid: String) : User?

    /**Writes user to realtime database
     * @see User
     */
    fun writeUser(user: User) {
        usersRef.child(authUser.uid).setValue(user).addOnFailureListener {
            Log.e("Firebase: writeUser()", it.message.toString())
        }.addOnCompleteListener {
            Log.d("Firebase: writeUser()", "Write is successful")
        }
    }

    /**Writes user to realtime database
     * @see User
     */
    fun writeUser(id: String, user: User) {
        usersRef.child(id).setValue(user).addOnFailureListener {
            Log.e("Firebase: writeUser()", it.message.toString())
        }.addOnCompleteListener {
            Log.d("Firebase: writeUser()", "Write is successful")
        }
    }

    /**Writes value into user field, specified by entry
     * @param field field entry
     * @see DbEntries
     */
    fun <T> writeField(field: String, value: T, uid: String = authUser.uid) {
        usersRef.child(uid).child(field).setValue(value).addOnFailureListener {
            Log.d("Firebase: writeField()", it.message.toString())
        }.addOnCompleteListener {
            Log.d("Firebase: writeField()", "Write is successful")
        }
    }


    /** Updates user fields. Function doesn't check database structure, use carefully.
     *  @param map map of path-value pairs
     */
    fun writeField(map: HashMap<String, Any>) {
        usersRef.child(authUser.uid).updateChildren(map)
    }


    /** Updates user field.
     * Function doesn't check database structure, use carefully.
     * @param path  path to field in user entry
     * @param value value for writing into specified path
     */
    fun <T> writeField(path: String, value: T) {
        val childUpdate = HashMap<String, Any>()
        childUpdate[path] = value as Any
        usersRef.child(authUser.uid).updateChildren(childUpdate)
    }


    /** Returns value from specified field
     * @param field field entry
     * @see DbEntries
     * @return value or null if field doesn't exist
     */
    suspend fun <T> getField(field: String, uid: String = authUser.uid): T? {
        val fieldData = usersRef.child(uid).child(field).getValue()
        @Suppress("UNCHECKED_CAST")
        return fieldData.value as T
    }


    /** Returns value from specified field
     * @param path  path to field in user entry
     * @return value or null if field doesn't exist
     */
    suspend fun <T> getField(path: String): T {
        var reference = usersRef.child(authUser.uid)
        val children = path.split("/")
        for (child in children) {
            reference = reference.child(child)
            Log.d("GetField", "new reference is $reference")
        }

        @Suppress("UNCHECKED_CAST")
        return reference.getValue().value as T
    }


    /** Checks if user exist in database table
     * @param uid user uid, by default uid from firebase authentication
     * @return true if user exist, else false
     */
    suspend fun isUserExist(uid: String = authUser.uid): Boolean {
        return usersRef.child(uid).exists()
    }


    /** Checks if field exist in user entry
     * @param field field entry
     * @see DbEntries
     * @return true if field exist in database, else false
     */
    suspend fun isFieldExist(field: String, uid: String = authUser.uid): Boolean {
        return usersRef.child(uid).child(field).exists()
    }


    /** Subscribes for value changes. Updates receives using callbacks in ValueEvenListener.
     * If updates aren't necessary anymore, don't forget to remove callbacks using
     * removeListeners() or removeAllListeners()
     * @param field field entry
     * @see DbEntries
     * @see ValueEventListener
     * @see removeAllListeners
     * @see removeListeners
     */
    fun subscribeForChanges(
        field: String,
        listener: ValueEventListener,
        uid: String = authUser.uid
    ) {
        val childRef = usersRef.child(uid).child(field)
        if (!eventListeners.contains(childRef)) {
            childRef.addValueEventListener(listener)
            eventListeners[childRef] = listener
            Log.d("Realtime database", "Set to listen for $childRef")
        } else {
            Log.e("Database Error", "Listener $childRef is already set")
        }
    }

    fun subscribeForChanges(
        table: String,
        entry: String,
        listener: ValueEventListener
    ) {
        val childRef = rootRef.child(table).child(entry)
        if (!eventListeners.contains(childRef)) {
            childRef.addValueEventListener(listener)
            eventListeners[childRef] = listener
            Log.d("Realtime database", "Set to listen for $childRef")
        } else {
            Log.e("Database Error", "Listener $childRef is already set")
        }
    }

    suspend fun getOrder(orderId: String): Order? {
        val orderData = ordersRef.child(orderId).getValue()
        return orderData.getValue(Order::class.java)
    }

    fun subscribeForOrder(
        orderId: String,
        listener: ValueEventListener
    ) {
        val ref = ordersRef.child(orderId)
        if (!eventListeners.contains(ref)) {
            ref.addValueEventListener(listener)
            eventListeners[ref] = listener
            Log.d("Realtime database", "Set to listen for $ref")
        } else {
            Log.e("Database Error", "Listener $ref is already set")
        }
    }

    fun subscribeForChanges(
        table: String,
        entry: String,
        field: String,
        listener: ValueEventListener
    ) {
        val childRef = rootRef.child(table).child(entry).child(field)
        if (!eventListeners.contains(childRef)) {
            childRef.addValueEventListener(listener)
            eventListeners[childRef] = listener
            Log.d("Realtime database", "Set to listen for $childRef")
        } else {
            Log.e("Database Error", "Listener $childRef is already set")
        }
    }

    /** Removes all active listeners
     * @see ValueEventListener
     * @see subscribeForChanges
     */
    fun removeAllListeners() {
        for (item in eventListeners) {
            when (item.value) {
                is ChildEventListener -> item.key.removeEventListener(item.value as ChildEventListener)
                is ValueEventListener -> item.key.removeEventListener(item.value as ValueEventListener)
            }
        }
        eventListeners.clear()
    }

    /** Removes listener, specified by entry
     * @param field field entry
     * @see DbEntries
     * @see ValueEventListener
     * @see subscribeForChanges
     */
    fun removeListeners(field: String, uid: String = authUser.uid) {
        removeListeners(usersRef.child(uid).child(field))
    }

    /** Removes listener, specified by entry
     * @param field field entry
     * @see DbEntries
     * @see ValueEventListener
     * @see subscribeForChanges
     */
    fun removeOrdersListeners(orderId: String) {
        removeListeners(ordersRef.child(orderId))
    }


    protected fun removeListeners(reference: DatabaseReference) {
        for (item in eventListeners) {
            if (item.key == reference) {
                Log.d("Realtime Database", "Unsubscribe from ${item.key}")
                when (item.value) {
                    is ChildEventListener -> item.key.removeEventListener(item.value as ChildEventListener)
                    is ValueEventListener -> item.key.removeEventListener(item.value as ValueEventListener)
                }
                eventListeners.remove(item.key)
            }
        }
    }
}