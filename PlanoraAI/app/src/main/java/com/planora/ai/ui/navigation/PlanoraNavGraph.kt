package com.planora.ai.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.planora.ai.R
import com.planora.ai.data.local.PlanoraDatabase
import com.planora.ai.data.repository.TaskRepository
import com.planora.ai.domain.planner.OfflinePlanningEngine
import com.planora.ai.ui.screens.calendar.CalendarScreen
import com.planora.ai.ui.screens.chat.ChatScreen
import com.planora.ai.ui.screens.goals.GoalsScreen
import com.planora.ai.ui.screens.home.HomeScreen
import com.planora.ai.ui.screens.onboarding.OnboardingScreen
import com.planora.ai.ui.screens.settings.SettingsScreen
import com.planora.ai.ui.screens.programs.ProgramsScreen
import com.planora.ai.ui.screens.tasks.TaskEditScreen
import com.planora.ai.ui.screens.habits.HabitsScreen
import com.planora.ai.ui.screens.stats.StatsScreen
import com.planora.ai.ui.screens.planpreview.PlanPreviewScreen

sealed class Screen(val route: String, val labelRes: Int, val icon: ImageVector, val selectedIcon: ImageVector) {
    data object Home : Screen("home", R.string.home, Icons.Outlined.Home, Icons.Filled.Home)
    data object Calendar : Screen("calendar", R.string.calendar, Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth)
    data object Chat : Screen("chat", R.string.ai_assistant, Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome)
    data object Goals : Screen("goals", R.string.goals, Icons.Outlined.Flag, Icons.Filled.Flag)
    data object Profile : Screen("profile", R.string.profile, Icons.Outlined.Person, Icons.Filled.Person)
}

val bottomNavItems = listOf(Screen.Home, Screen.Calendar, Screen.Chat, Screen.Goals, Screen.Profile)

@Composable
fun PlanoraNavGraph(
    database: PlanoraDatabase,
    offlineEngine: OfflinePlanningEngine,
    taskRepository: TaskRepository
) {
    val navController = rememberNavController()
    val prefs by database.preferencesDao().getPreferences().collectAsState(initial = null)
    val hasOnboarded = prefs?.hasCompletedOnboarding == true

    val startDestination = if (hasOnboarded) Screen.Home.route else "onboarding"

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) screen.selectedIcon else screen.icon,
                                    contentDescription = stringResource(screen.labelRes)
                                )
                            },
                            label = { Text(stringResource(screen.labelRes)) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    database = database,
                    onFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    taskRepository = taskRepository,
                    database = database,
                    onAddTask = { navController.navigate("task_edit") },
                    onEditTask = { id -> navController.navigate("task_edit/$id") },
                    onOpenStats = { navController.navigate("stats") }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    taskRepository = taskRepository,
                    onTaskClick = { id -> navController.navigate("task_edit/$id") }
                )
            }
            composable(Screen.Chat.route) {
                ChatScreen(
                    database = database,
                    offlineEngine = offlineEngine,
                    onPlanReady = { planJson -> navController.navigate("plan_preview") }
                )
            }
            composable(Screen.Goals.route) {
                GoalsScreen(database = database)
            }
            composable(Screen.Profile.route) {
                SettingsScreen(
                    database = database,
                    onOpenHabits = { navController.navigate("habits") },
                    onOpenStats = { navController.navigate("stats") },
                    onOpenPrograms = { navController.navigate("programs") }
                )
            }
            composable("programs") {
                ProgramsScreen(
                    database = database,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("task_edit") {
                TaskEditScreen(
                    taskId = null,
                    taskRepository = taskRepository,
                    onDone = { navController.popBackStack() }
                )
            }
            composable("task_edit/{taskId}") { backStack ->
                val id = backStack.arguments?.getString("taskId")?.toLongOrNull()
                TaskEditScreen(
                    taskId = id,
                    taskRepository = taskRepository,
                    onDone = { navController.popBackStack() }
                )
            }
            composable("habits") {
                HabitsScreen(database = database, onBack = { navController.popBackStack() })
            }
            composable("stats") {
                StatsScreen(taskRepository = taskRepository, onBack = { navController.popBackStack() })
            }
            composable("plan_preview") {
                PlanPreviewScreen(
                    onAccept = { navController.popBackStack() },
                    onReject = { navController.popBackStack() }
                )
            }
        }
    }
}
