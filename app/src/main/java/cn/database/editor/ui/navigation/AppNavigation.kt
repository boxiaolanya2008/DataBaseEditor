package cn.database.editor.ui.navigation

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import cn.database.editor.ui.screen.AppListScreen
import cn.database.editor.ui.screen.DatabaseSchemaScreen
import cn.database.editor.ui.screen.HomeScreen
import cn.database.editor.ui.screen.QueryEditorScreen
import cn.database.editor.ui.screen.SettingsScreen
import cn.database.editor.ui.screen.TableDataScreen
import cn.database.editor.ui.screen.TableListScreen
import cn.database.editor.ui.screen.TableStructureScreen
import cn.database.editor.ui.screen.TutorialScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController,
    hasShownTutorial: Boolean,
    onTutorialComplete: () -> Unit,
    drawerState: DrawerState
) {
    val scope = rememberCoroutineScope()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route ?: ""

    val showDrawer = currentRoute in listOf(Screen.Home.route, Screen.Settings.route)

    if (showDrawer) {
        androidx.compose.material3.ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerSheet(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    drawerState = drawerState,
                    scope = scope
                )
            }
        ) {
            NavHostContent(
                navController = navController,
                hasShownTutorial = hasShownTutorial,
                onTutorialComplete = onTutorialComplete,
                drawerState = drawerState
            )
        }
    } else {
        NavHostContent(
            navController = navController,
            hasShownTutorial = hasShownTutorial,
            onTutorialComplete = onTutorialComplete,
            drawerState = drawerState
        )
    }
}

@Composable
private fun NavHostContent(
    navController: NavHostController,
    hasShownTutorial: Boolean,
    onTutorialComplete: () -> Unit,
    drawerState: DrawerState
) {
    val startDestination = if (hasShownTutorial) Screen.Home.route else Screen.Tutorial.route
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Tutorial.route) {
            TutorialScreen(
                onComplete = {
                    onTutorialComplete()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Tutorial.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onDatabaseSelected = { dbPath ->
                    navController.navigate(Screen.TableList.createRoute(dbPath))
                },
                onOpenDrawer = {
                    scope.launch {
                        drawerState.open()
                    }
                },
                onOpenAppList = {
                    navController.navigate(Screen.AppList.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AppList.route) {
            AppListScreen(
                onDatabaseSelected = { dbPath ->
                    navController.navigate(Screen.TableList.createRoute(dbPath))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TableList.route) { backStackEntry ->
            val dbPath = backStackEntry.arguments?.getString("dbPath")?.decode() ?: ""
            TableListScreen(
                dbPath = dbPath,
                onTableClick = { tableName ->
                    navController.navigate(Screen.TableData.createRoute(dbPath, tableName))
                },
                onTableStructureClick = { tableName ->
                    navController.navigate(Screen.TableStructure.createRoute(dbPath, tableName))
                },
                onQueryEditorClick = {
                    navController.navigate(Screen.QueryEditor.createRoute(dbPath))
                },
                onSchemaClick = {
                    navController.navigate(Screen.DatabaseSchema.createRoute(dbPath))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TableData.route) { backStackEntry ->
            val dbPath = backStackEntry.arguments?.getString("dbPath")?.decode() ?: ""
            val tableName = backStackEntry.arguments?.getString("tableName")?.decode() ?: ""
            TableDataScreen(
                dbPath = dbPath,
                tableName = tableName,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TableStructure.route) { backStackEntry ->
            val dbPath = backStackEntry.arguments?.getString("dbPath")?.decode() ?: ""
            val tableName = backStackEntry.arguments?.getString("tableName")?.decode() ?: ""
            TableStructureScreen(
                dbPath = dbPath,
                tableName = tableName,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QueryEditor.route) { backStackEntry ->
            val dbPath = backStackEntry.arguments?.getString("dbPath")?.decode() ?: ""
            QueryEditorScreen(
                dbPath = dbPath,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DatabaseSchema.route) { backStackEntry ->
            val dbPath = backStackEntry.arguments?.getString("dbPath")?.decode() ?: ""
            DatabaseSchemaScreen(
                dbPath = dbPath,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
