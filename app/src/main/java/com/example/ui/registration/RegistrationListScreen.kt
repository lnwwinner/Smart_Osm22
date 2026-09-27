package com.example.ui.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Person
import com.example.viewmodel.PersonViewModel

@Composable
fun RegistrationListScreen(
    viewModel: PersonViewModel,
    onPersonClick: (Person) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allPersons by viewModel.allPersons.collectAsStateWithLifecycle()

    val filteredPersons by remember(allPersons, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                allPersons
            } else {
                allPersons.filter {
                    it.fullName.contains(searchQuery, ignoreCase = true) ||
                            (it.nationalId?.contains(searchQuery) ?: false)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("search_bar"),
                placeholder = { Text("ค้นหาชื่อ หรือ เลขบัตร...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("person_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredPersons, key = { it.id }) { person ->
                PersonListItem(person = person, onClick = { onPersonClick(person) })
            }
        }
    }
}

@Composable
fun PersonListItem(
    person: Person,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("person_card"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = person.fullName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "ID: ${person.nationalId ?: "ไม่มีข้อมูล"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
