package com.oguzhanozdemir.readupenglish.view

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.oguzhanozdemir.readupenglish.database.WordDao

@Composable
fun AppNavGraph(navController: NavHostController){
      NavHost(navController = navController, startDestination = "home_screen"){
          composable("book_list"){
              BookScreen(navController = navController)
          }

          composable("book_detail/{bookId}") { backStackEntry ->
              val bookId = backStackEntry.arguments?.getString("bookId")?.toIntOrNull()
              if (bookId != null) {
                  BookDetailScreen(bookId = bookId, navController = navController)
              }
          }

          composable("word_list_screen"){WordListScreen()}
          composable("home_screen"){HomeScreen(navController = navController)}
          composable("favorite_book_list"){FavoriteBookList(navController = navController)}


      }



}