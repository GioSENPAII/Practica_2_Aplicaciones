// Crear un nuevo archivo en app/src/main/java/com/example/practicacrud/adapters/UserAdapter.kt
package com.example.practicacrud.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.practicacrud.R
import com.example.practicacrud.models.UserResponse

class UserAdapter(
    private val userList: List<UserResponse>,
    private val onEditClick: (UserResponse) -> Unit,
    private val onDeleteClick: (UserResponse) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvName)
        private val tvRole: TextView = itemView.findViewById(R.id.tvDescription)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(user: UserResponse) {
            tvUsername.text = user.username
            tvRole.text = "Rol: ${user.role}"

            btnEdit.setOnClickListener { onEditClick(user) }
            btnDelete.setOnClickListener { onDeleteClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size
}