package com.oguzhanozdemir.readupenglish.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun HomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ReadUp English",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Kitaplara Göz At",
                fontSize = 20.sp,
                modifier = Modifier.clickable {
                    navController.navigate("book_list")
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Kayıtlı Kelimeler",
                fontSize = 20.sp,
                modifier = Modifier.clickable {
                    navController.navigate("word_list_screen")
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Favori Kitaplar",
                fontSize = 20.sp,
                modifier = Modifier.clickable {
                    navController.navigate("favorite_book_list")
                }
            )

        }
    }
}