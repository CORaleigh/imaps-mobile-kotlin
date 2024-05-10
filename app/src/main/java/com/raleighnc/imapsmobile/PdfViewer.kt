
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier

import androidx.navigation.NavController
import com.raleighnc.imapsmobile.HideableBottomSheetState
import com.raleighnc.imapsmobile.TopBar
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.rememberVerticalPdfReaderState
import com.rizzi.bouquet.VerticalPDFReader

@Composable
fun PdfViewer(url: String, bottomSheetState: HideableBottomSheetState, navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val pdfState = rememberVerticalPdfReaderState(
        resource = ResourceType.Remote(url),
        isZoomEnable = true
    )
    Scaffold(
        topBar = {
            TopBar(
                title = "Deed",
                bottomSheetState = bottomSheetState,
                coroutineScope = coroutineScope,
                navController = navController
            )
        }
    ) {
        VerticalPDFReader(
            state = pdfState,
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(color = MaterialTheme.colorScheme.background)
        )
    }
}
