package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CinemaColorTheme
import com.example.data.Film
import com.example.data.FilmRepository
import com.example.ui.components.FeaturedHeroBanner
import com.example.ui.components.FilmPosterCard
import com.example.ui.components.NightOfLivingDeadVaultSection
import com.example.ui.components.UploadedFilmCard
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.DarkNavyBackground
import com.example.ui.theme.DarkNavySurface

@Composable
fun FilmHomeScreen(
    films: List<Film>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilmClick: (Film) -> Unit,
    onPlayClick: (Film) -> Unit,
    onToggleFavorite: (Film) -> Unit,
    onDeleteFilm: (Film) -> Unit,
    onUploadClick: () -> Unit,
    isVipMember: Boolean,
    userEmail: String,
    onOpenPaystack: () -> Unit,
    onOpenSharePromo: () -> Unit,
    onOpenExchange: () -> Unit,
    onOpenTwilio: () -> Unit,
    onOpenInsideSettings: () -> Unit,
    currentColorTheme: CinemaColorTheme = CinemaColorTheme.ROYAL_GOLD,
    onColorThemeSelected: (CinemaColorTheme) -> Unit = {},
    isBigScreenMode: Boolean = true,
    onToggleBigScreenMode: () -> Unit = {},
    userName: String = "Mopolee Member",
    onLogout: () -> Unit = {},
    onOpenLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isGridCardsSmall by remember { mutableStateOf(false) }

    val filteredFilms = remember(films, selectedCategory, searchQuery) {
        films.filter { film ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "My Uploads" -> film.isUserUpload
                else -> film.genre.equals(selectedCategory, ignoreCase = true) ||
                        (selectedCategory == "Night of the Living Dead" &&
                                (film.seriesTitle == "Night of the Living Dead" || film.title.contains("Night of the Living Dead", ignoreCase = true)))
            }
            val matchesQuery = searchQuery.isBlank() ||
                    film.title.contains(searchQuery, ignoreCase = true) ||
                    film.synopsis.contains(searchQuery, ignoreCase = true) ||
                    film.genre.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    val featuredFilm = remember(films) {
        films.firstOrNull { it.id == "notld_ep_1" } ?: films.firstOrNull()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onUploadClick,
                containerColor = currentColorTheme.primaryColor,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Upload Film")
            }
        },
        containerColor = currentColorTheme.backgroundColor
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top App Bar & Quick Sizing Toggle
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "MOPOLEE CINEMA",
                                color = currentColorTheme.primaryColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Night of the Living Dead • Episodes 1 to 32",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onOpenLogin) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Account / Login", tint = Color.White)
                            }
                            IconButton(onClick = onOpenInsideSettings) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = currentColorTheme.primaryColor)
                            }
                            IconButton(onClick = onOpenSharePromo) {
                                Icon(Icons.Default.Share, contentDescription = "Social Promo", tint = Color.White)
                            }
                        }
                    }

                    // "BIG / SMALL" Screen Sizing Header Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = currentColorTheme.surfaceColor,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, currentColorTheme.cardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SCREEN SIZE:",
                                    color = currentColorTheme.primaryColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Big button
                                Surface(
                                    onClick = { if (!isBigScreenMode) onToggleBigScreenMode() },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isBigScreenMode) currentColorTheme.primaryColor else Color.Transparent
                                ) {
                                    Text(
                                        text = "📱 BIG (9:16)",
                                        color = if (isBigScreenMode) Color.Black else Color.White,
                                        fontWeight = if (isBigScreenMode) FontWeight.ExtraBold else FontWeight.Normal,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                // Small button
                                Surface(
                                    onClick = { if (isBigScreenMode) onToggleBigScreenMode() },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (!isBigScreenMode) currentColorTheme.primaryColor else Color.Transparent
                                ) {
                                    Text(
                                        text = "🎬 SMALL (16:9)",
                                        color = if (!isBigScreenMode) Color.Black else Color.White,
                                        fontWeight = if (!isBigScreenMode) FontWeight.ExtraBold else FontWeight.Normal,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Card size toggle
                            IconButton(
                                onClick = { isGridCardsSmall = !isGridCardsSmall },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    if (isGridCardsSmall) Icons.Default.ViewAgenda else Icons.Default.GridView,
                                    contentDescription = "Card Size Toggle",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search 32 episodes, movies, genres...", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = currentColorTheme.primaryColor) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = currentColorTheme.surfaceColor,
                            unfocusedContainerColor = currentColorTheme.surfaceColor,
                            focusedBorderColor = currentColorTheme.primaryColor,
                            unfocusedBorderColor = currentColorTheme.cardBorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )
                }
            }

            // Category Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FilmRepository.categories) { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelected(cat) },
                            label = {
                                Text(
                                    text = cat,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = currentColorTheme.primaryColor,
                                selectedLabelColor = Color.Black,
                                containerColor = currentColorTheme.surfaceColor,
                                labelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = currentColorTheme.cardBorderColor,
                                selectedBorderColor = currentColorTheme.primaryColor
                            )
                        )
                    }
                }
            }

            // Hero Banner: Episode 1 of Night of the Living Dead
            if (featuredFilm != null && searchQuery.isBlank() && selectedCategory == "All") {
                item {
                    FeaturedHeroBanner(
                        film = featuredFilm,
                        onPlayClick = { onPlayClick(featuredFilm) },
                        onDetailsClick = { onFilmClick(featuredFilm) },
                        onToggleFavorite = onToggleFavorite,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // NIGHT OF THE LIVING DEAD VAULT SECTION (All 32 Episodes)
            if (selectedCategory == "All" || selectedCategory == "Night of the Living Dead") {
                item {
                    NightOfLivingDeadVaultSection(
                        films = films,
                        onSelectFilm = onFilmClick,
                        onPlayFilm = onPlayClick,
                        currentColorTheme = currentColorTheme,
                        ambientGlow = true,
                        onOpenInsideSettings = onOpenInsideSettings,
                        isVipMember = isVipMember
                    )
                }
            }

            // Quick Services Row (WhatsApp AI & SMS Hub & VIP Pass)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onOpenExchange,
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF25D366).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("WhatsApp Desk", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("07088193492", color = Color(0xFF25D366), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }

                    Surface(
                        onClick = onOpenTwilio,
                        shape = RoundedCornerShape(14.dp),
                        color = currentColorTheme.primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, currentColorTheme.primaryColor.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null, tint = currentColorTheme.primaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("SMS & OTP Hub", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Twilio Live", color = currentColorTheme.primaryColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // All Movies / Episodes List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (selectedCategory == "All") "ALL FILMS & EPISODES (${filteredFilms.size})" else "$selectedCategory (${filteredFilms.size})",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )

                    Surface(
                        onClick = onOpenPaystack,
                        color = if (isVipMember) Color(0xFF00E676) else currentColorTheme.primaryColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isVipMember) "👑 VIP Active" else "⚡ VIP Pass (₦2,500)",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Film Poster Row / Grid
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredFilms, key = { it.id }) { film ->
                        FilmPosterCard(
                            film = film,
                            onClick = { onFilmClick(film) },
                            onPlayClick = { onPlayClick(film) }
                        )
                    }
                }
            }

            // If user has uploaded films
            val userUploads = films.filter { it.isUserUpload }
            if (userUploads.isNotEmpty()) {
                item {
                    Text(
                        text = "MY UPLOADED FILMS",
                        color = currentColorTheme.primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                items(userUploads, key = { it.id }) { upload ->
                    UploadedFilmCard(
                        film = upload,
                        onPlayClick = { onPlayClick(upload) },
                        onDeleteClick = { onDeleteFilm(upload) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
