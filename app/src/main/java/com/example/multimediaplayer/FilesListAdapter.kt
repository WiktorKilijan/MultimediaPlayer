package com.example.multimediaplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FilesListAdapter(
    context: Context,
    filesList: ArrayList<File>,
    favoritesHelper: FavoritesHelper
) : RecyclerView.Adapter<FilesListAdapter.FilesViewHolder>() {

    var filesList: ArrayList<File> = filesList
    private var filesListCopy = ArrayList<File>()

    private var favoritesHelper = favoritesHelper
    private var context = context
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    init {
        filesListCopy.addAll(filesList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesViewHolder {
        val view = mInflater.inflate(R.layout.image_item, parent, false)
        return FilesViewHolder(view, filesList, favoritesHelper, context, this)
    }

    override fun onBindViewHolder(holder: FilesViewHolder, position: Int) {
        holder.fileName.text = filesList[position].name

        if (isSoundRecordFromPath(filesList[position].absolutePath)) {
            holder.image.setImageResource(R.drawable.microphone)
        } else if (isVideoFromPath(filesList[position].absolutePath)) {
            holder.image.setImageResource(R.drawable.clapperboard)
        } else if (isImageFromPath(filesList[position].absolutePath)) {
            holder.image.setImageURI(filesList[position].absolutePath.toUri())
        }

        val favoritesSet = favoritesHelper.getFavoriteFiles()
        if (favoritesSet.contains(filesList[position].name)) {
            holder.star.setImageResource(R.drawable.star_full)
        } else {
            holder.star.setImageResource(R.drawable.star_empty)
        }

    }

    override fun getItemCount(): Int {
        return filesList.size
    }

    fun filter(text: String?) {
        filesList.clear()
        if (text != null) {
            if (text.isEmpty()) {
                filesList.addAll(filesListCopy)
            } else {
                var textLower = text.toLowerCase()
                for (file in filesListCopy) {
                    if (file.name.toLowerCase().contains(textLower)) {
                        filesList.add(file)
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    companion object {
        fun isImageFromPath(path: String): Boolean {
            return path.endsWith(".jpg") or path.endsWith(".png")
        }

        fun isSoundRecordFromPath(path: String): Boolean {
            return path.endsWith(".mp3")
        }

        fun isVideoFromPath(path: String): Boolean {
            return path.endsWith(".avi")
        }
    }

    class FilesViewHolder(
        view: View,
        filesList: MutableList<File>,
        favoritesHelper: FavoritesHelper,
        context: Context,
        adapter: FilesListAdapter
    ) :
        RecyclerView.ViewHolder(view), View.OnLongClickListener, View.OnClickListener {
        var context: Context
        var image: ImageView
        var star: ImageView

        var fileName: TextView
        var filesList: MutableList<File>
        var favoritesHelper: FavoritesHelper
        private var adapter: FilesListAdapter


        override fun onLongClick(view: View): Boolean {
            deleteFile(view)
            return false
        }

        private fun deleteFile(view: View) {
            val position = adapterPosition
            Toast.makeText(context, context.getString(R.string.deleted), Toast.LENGTH_SHORT).show()
            val file: File = File(filesList[position].absolutePath)
            if (file.delete()) {
                // Default? Main?
//                CoroutineScope(Dispatchers.Default).launch {
//                    var database = FilesDatabase.getDatabase(context)
//                    database.filesDao().delete(filesList[position])
//                }

                filesList.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            val filePath = filesList[position].absolutePath
            if (isImageFromPath(filePath)) {
                val intent = Intent(view.context, PhotoDetailsActivity::class.java)
                intent.putExtra("photoURI", filesList[position].absolutePath)
                context.startActivity(intent)
            } else if (isSoundRecordFromPath(filePath) || isVideoFromPath(filePath)) {
                val intent = Intent(view.context, PlayerActivity::class.java)
                intent.putExtra("fileURI", filesList[position].absolutePath)
                context.startActivity(intent)
            }
        }


        private fun starClicked(view: ImageView) {
            val favoritesSet = favoritesHelper.getFavoriteFiles()

            if (!favoritesSet.contains(filesList[adapterPosition].name)) {
                view.setImageResource(R.drawable.star_full)
                addToFavorites()
            } else {
                view.setImageResource(R.drawable.star_empty)
                removeFromFavorites()
            }
        }

        private fun addToFavorites() {
            favoritesHelper.addFileToFavorite(filesList[adapterPosition])
        }

        private fun removeFromFavorites() {
            favoritesHelper.removeFromFavorite(filesList[adapterPosition])
        }

        init {
            view.setOnLongClickListener(this)
            view.setOnClickListener(this)
            this.context = context
            this.image = view.findViewById(R.id.item_imageView)
            this.fileName = view.findViewById(R.id.item_name_textView)
            this.star = view.findViewById(R.id.favoriteImageView)

            star.setOnClickListener {
                starClicked(it as ImageView)
            }

            this.filesList = filesList
            this.adapter = adapter
            this.favoritesHelper = favoritesHelper
        }
    }


}