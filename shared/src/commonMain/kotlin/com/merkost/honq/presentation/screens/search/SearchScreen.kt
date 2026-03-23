package com.merkost.honq.presentation.screens.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.merkost.honq.domain.model.Question
import com.merkost.honq.presentation.components.base.AnimatedFavoriteButton
import com.merkost.honq.presentation.components.base.HonqCard
import com.merkost.honq.presentation.components.base.HonqScaffold
import com.merkost.honq.presentation.theme.HonqMotion
import com.merkost.honq.presentation.theme.HonqSizing
import com.merkost.honq.presentation.theme.HonqSpacing
import com.merkost.honq.presentation.theme.HonqTheme
import org.koin.compose.viewmodel.koinViewModel
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuestion: (questionId: String) -> Unit
) {
    val container = koinViewModel<SearchContainer>()

    val state by container.store.subscribe { action ->
        when (action) {
            SearchAction.NavigateBack -> onNavigateBack()
            is SearchAction.NavigateToQuestion -> onNavigateToQuestion(action.questionId)
        }
    }

    SearchContent(
        state = state,
        onIntent = container.store::intent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun SearchContent(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val colors = HonqTheme.colors
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    HonqScaffold(
        title = "Search Questions",
        onNavigateBack = onNavigateBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = state.query,
                onQueryChange = { onIntent(SearchIntent.UpdateQuery(it)) },
                onClear = { onIntent(SearchIntent.ClearQuery) },
                onSearch = { keyboardController?.hide() },
                isSearching = state.isSearching,
                focusRequester = focusRequester,
                modifier = Modifier.padding(HonqSizing.screenPadding)
            )

            val contentKey = when {
                state.error != null -> SearchContentKey.Error
                state.isEmpty -> SearchContentKey.Empty
                state.results.isNotEmpty() -> SearchContentKey.Results
                state.query.isBlank() -> SearchContentKey.Prompt
                state.query.length < 2 -> SearchContentKey.MinChars
                state.isSearching -> SearchContentKey.Searching
                else -> SearchContentKey.Prompt
            }

            AnimatedContent(
                targetState = contentKey,
                transitionSpec = {
                    fadeIn(HonqMotion.tweenMedium())
                        .togetherWith(fadeOut(HonqMotion.tweenShort()))
                },
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { key ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (key) {
                        SearchContentKey.Searching -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = HonqSpacing.xl),
                                color = colors.loadingIndicator
                            )
                        }
                        SearchContentKey.Error -> {
                            Text(
                                text = state.error.orEmpty(),
                                color = colors.incorrect,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(horizontal = HonqSizing.screenPadding)
                                    .padding(top = HonqSpacing.lg)
                            )
                        }
                        SearchContentKey.Empty -> {
                            EmptySearchState(
                                query = state.query,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                        SearchContentKey.Results -> {
                            SearchResults(
                                results = state.results,
                                favoriteIds = state.favoriteQuestionIds,
                                onQuestionClick = { onIntent(SearchIntent.SelectQuestion(it)) },
                                onToggleFavorite = { onIntent(SearchIntent.ToggleFavorite(it)) }
                            )
                        }
                        SearchContentKey.Prompt -> {
                            SearchPrompt(modifier = Modifier.align(Alignment.TopCenter))
                        }
                        SearchContentKey.MinChars -> {
                            Text(
                                text = "Type at least 2 characters to search",
                                color = colors.textMuted,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = HonqSpacing.lg)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HonqSizing.cornerRadius))
            .background(colors.surface)
            .padding(horizontal = HonqSpacing.md, vertical = HonqSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(HonqSizing.iconSizeSmall)
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textPrimary
            ),
            cursorBrush = SolidColor(colors.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search by keyword or question ID...",
                            color = colors.textMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    innerTextField()
                }
            }
        )

        AnimatedVisibility(
            visible = query.isNotEmpty() || isSearching,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.size(HonqSizing.iconSizeMedium),
                contentAlignment = Alignment.Center
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(HonqSizing.iconSizeSmall),
                        color = colors.loadingIndicator,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(HonqSizing.iconSizeMedium)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear",
                            tint = colors.textMuted,
                            modifier = Modifier.size(HonqSizing.iconSizeSmall)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResults(
    results: List<Question>,
    favoriteIds: Set<String>,
    onQuestionClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    val colors = HonqTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = HonqSizing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(HonqSpacing.sm)
    ) {
        item {
            Text(
                text = "${results.size} result${if (results.size != 1) "s" else ""} found",
                color = colors.textMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = HonqSpacing.xs)
            )
        }

        items(results, key = { it.id }) { question ->
            SearchResultCard(
                question = question,
                isFavorite = favoriteIds.contains(question.id),
                onClick = { onQuestionClick(question.id) },
                onToggleFavorite = { onToggleFavorite(question.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(HonqSpacing.lg))
        }
    }
}

@Composable
private fun SearchResultCard(
    question: Question,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val colors = HonqTheme.colors

    HonqCard(
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = question.text,
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(HonqSpacing.xs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(HonqSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = question.categoryName,
                        color = colors.textMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "•",
                        color = colors.textMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "#${question.code}",
                        color = colors.textMuted.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            AnimatedFavoriteButton(
                isFavorite = isFavorite,
                onClick = onToggleFavorite,
                modifier = Modifier.size(HonqSizing.iconSizeLarge)
            )
        }
    }
}

@Composable
private fun SearchPrompt(modifier: Modifier = Modifier) {
    val colors = HonqTheme.colors

    Column(
        modifier = modifier
            .padding(horizontal = HonqSizing.screenPadding)
            .padding(top = HonqSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(HonqSizing.iconSizeLarge)
        )
        Spacer(modifier = Modifier.height(HonqSpacing.sm))
        Text(
            text = "Search for questions",
            color = colors.textSecondary,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(HonqSpacing.xs))
        Text(
            text = "Find questions by keywords, question ID, or answer text",
            color = colors.textMuted,
            style = MaterialTheme.typography.labelMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private enum class SearchContentKey {
    Prompt, MinChars, Searching, Error, Empty, Results
}

@Composable
private fun EmptySearchState(
    query: String,
    modifier: Modifier = Modifier
) {
    val colors = HonqTheme.colors

    Column(
        modifier = modifier
            .padding(horizontal = HonqSizing.screenPadding)
            .padding(top = HonqSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No results found",
            color = colors.textSecondary,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(HonqSpacing.xs))
        Text(
            text = "No questions match \"$query\". Try a different search term.",
            color = colors.textMuted,
            style = MaterialTheme.typography.labelMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
