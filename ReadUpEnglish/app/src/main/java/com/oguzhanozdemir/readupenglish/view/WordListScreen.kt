package com.oguzhanozdemir.readupenglish.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.oguzhanozdemir.readupenglish.database.WordDao
import com.oguzhanozdemir.readupenglish.model.WordEntity
import com.oguzhanozdemir.readupenglish.viewmodel.TranslateViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(viewModel: TranslateViewModel = hiltViewModel()) {
    val wordList by viewModel.savedWords.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Kayıtlı Kelimeler") })
        }
    ) { paddingValues ->
        if (wordList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Henüz kayıtlı kelime yok.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(wordList) { word ->
                    WordItem(word)
                }
            }
        }
    }
}

@Composable
fun WordItem(word: WordEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(text = word.word, style = MaterialTheme.typography.titleMedium)
        Text(text = word.translation, style = MaterialTheme.typography.bodyMedium)
    }
}