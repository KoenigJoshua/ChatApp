package com.koenig.chatapp.ui.contactsManager


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.koenig.chatapp.adapters.ContactsAdapter
import com.koenig.chatapp.adapters.ContactsClickListener
import com.koenig.chatapp.databinding.FragmentContactsBinding
import com.koenig.chatapp.models.UserModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel

class ContactsFragment : Fragment(), ContactsClickListener {

    private var _fragBinding: FragmentContactsBinding? = null
    private val fragBinding get() = _fragBinding!!

    private val contactsViewModel: ContactsViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentContactsBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.recyclerViewMyContacts.layoutManager = LinearLayoutManager(activity)

        contactsViewModel.observableContacts.observe(viewLifecycleOwner, Observer { contacts ->
            contacts?.let {
                render(contacts as ArrayList<UserModel>)
            }
        })

        fragBinding.buttonSearchContacts.setOnClickListener {
            val action = ContactsFragmentDirections.actionContactsFragmentToSearchContactsFragment()
            findNavController().navigate(action)
        }

        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                contactsViewModel.loadAllContacts(firebaseUser.uid)
            }
        }

        return  root
    }

    private fun render(contacts: ArrayList<UserModel>)
    {
        fragBinding.recyclerViewMyContacts.adapter = ContactsAdapter(contacts, this)

        if(contacts.isEmpty())
        {
            fragBinding.recyclerViewMyContacts.visibility = View.GONE
            fragBinding.textNoContacts.visibility = View.VISIBLE
        }
        else
        {
            fragBinding.recyclerViewMyContacts.visibility = View.VISIBLE
            fragBinding.textNoContacts.visibility = View.GONE
        }
    }

    override fun onClickOpenChat(selectedUser: UserModel) {
        val action = ContactsFragmentDirections.actionContactsFragmentToChatFragment(selectedUser)
        findNavController().navigate(action)
    }
}