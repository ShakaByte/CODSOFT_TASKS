package com.example.quoteoftheday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.quoteoftheday.ui.theme.QuoteOfTheDayTheme
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

data class Quote(val text: String , val author: String)
fun loadQuotes(context:Context):List<Quote> {
    val jsonString = context.assets.open("quotes.json")
        .bufferedReader()
        .use { it.readText() }
    val jsonArray = org.json.JSONArray(jsonString)
    val quotes = mutableListOf<Quote>()

    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        quotes.add(Quote(text = obj.getString("quote"), author = obj.getString("author")))
    }
    return quotes
}
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuoteOfTheDayTheme {
                val navController = rememberNavController()
                var favorites by remember { mutableStateOf(listOf<Quote>()) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "welcome",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("welcome") {
                            WelcomeScreen(
                                modifier = Modifier,
                                onGetStartedClick = {
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                favorites = favorites,
                                onAddFavorite = { quote -> if (!favorites.contains(quote)) favorites = favorites + quote },
                                onFavoritesClick = { navController.navigate("favorites") }
                            )
                        }
                        composable("favorites") {
                            Scaffold(
                                topBar = {
                                    TopAppBar(
                                        title = { Text("Favorites") },
                                        navigationIcon = {
                                            IconButton(onClick = { navController.popBackStack() }) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                            }
                                        }
                                    )
                                }
                            ) { innerPadding ->
                                FavScreen(
                                    favorites = favorites,
                                    onRemoveFavorite = { quote ->
                                        favorites = favorites - quote
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier, onGetStartedClick: () -> Unit = {}) {
    var nameInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome! What's your name?",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onGetStartedClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }
    }
}

@Composable
fun HomeScreen(favorites: List<Quote>,
               onAddFavorite: (Quote) -> Unit,
               onFavoritesClick: () -> Unit,
               modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val quotes = remember { loadQuotes(context) }
    var currentQuote by remember { mutableStateOf(quotes.random()) }

    Column(
        modifier = modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = currentQuote.text,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "— ${currentQuote.author}",
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth())

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { currentQuote = quotes.random() }, modifier = Modifier.weight(1f)) {
                Text("Refresh")
            }
            Button(onClick = { onAddFavorite(currentQuote) }, modifier = Modifier.weight(1f)) {
                Text("Add")
            }
            Button(onClick = {
                val shareText = "${currentQuote.text} — ${currentQuote.author}"
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            }, modifier = Modifier.weight(1f)) {
                Text("Share")
            }
        }

        Button(onClick = onFavoritesClick, modifier = Modifier.fillMaxWidth()) {
            Text("View Favorites")
        }
    }
}

@Composable
fun FavScreen(
    favorites: List<Quote>,
    onRemoveFavorite: (Quote) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(favorites) { quote ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = quote.text)
                Text(text = "— ${quote.author}")
                Button(onClick = { onRemoveFavorite(quote) }) {
                    Text("Remove")
                }
            }
            HorizontalDivider()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    QuoteOfTheDayTheme {
        WelcomeScreen()
    }
}