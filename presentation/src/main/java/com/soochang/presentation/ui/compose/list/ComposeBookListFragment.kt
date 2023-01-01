package com.soochang.presentation.ui.compose.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import androidx.paging.compose.itemsIndexed
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.soochang.domain.model.book.BookItem
import com.soochang.presentation.R
import com.soochang.presentation.config.compose.theme.AndroidTrainingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow

@AndroidEntryPoint
class ComposeBookListFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val navController = findNavController(this)

        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AndroidTrainingTheme{
                    MainScreen(navController)
                }
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavController, viewModel: ComposeBookListViewModel = hiltViewModel()){
    //이벤트 발행
    val onBackPress: () -> Unit = {
        viewModel.setEvent(
            ComposeBookListContract.Event.OnBackPressed
        )
    }

    val onSearchAction = { query: String ->
        viewModel.setEvent(
            ComposeBookListContract.Event.OnSearchAction(query)
        )
    }

    val onBookClicked: (id: String) -> Unit = { id ->
        viewModel.setEvent(
            ComposeBookListContract.Event.OnBookClicked(id)
        )
    }

    val onLoadStateChanged: (loadState: CombinedLoadStates, itemCount: Int) -> Unit = { loadState, itemCount ->
        viewModel.setEvent(
            ComposeBookListContract.Event.OnLoadStateChanged(loadState, itemCount)
        )
    }



    val focusManager = LocalFocusManager.current
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(onBackPress, onSearchAction)
        },
        modifier = Modifier
            .pointerInput(Unit) {//TextField외곽 터치시 키보드 숨기기
                detectTapGestures(onPress = {
                    focusManager.clearFocus()
                })
            }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
            content = {
                //Effect수신
                //recompose SharedFlow의 값이 전달된 경우에만 수행되도록 LaunchedEffect블럭 내에서 collect
                LaunchedEffect(true) {
                    viewModel.effect.collect{ uiEffect ->
                        when (uiEffect) {
                            is ComposeBookListContract.Effect.PopBackStack -> {
                                navController.popBackStack()
                            }
                            is ComposeBookListContract.Effect.NavigateBookDetailScreen -> {
                                //화면이동
                                val action = ComposeBookListFragmentDirections.actionComposeBookListFragmentToGoogleBookDetailFragment(uiEffect.id)
                                navController.navigate(action)
                            }
                            is ComposeBookListContract.Effect.ScrollToTop -> {
                                lazyListState.scrollToItem(0)
                            }
                            is ComposeBookListContract.Effect.ShowErrorSnackbar -> {
                                scaffoldState.snackbarHostState.showSnackbar(context.getString(uiEffect.messageId))
                            }
                        }
                    }
                }

                BookListPage(viewModel.pagingData, lazyListState, onLoadStateChanged, onBookClicked)

                //State수신
                val uiState by viewModel.uiState.collectAsState()

                when{
                    uiState.showInitialPage -> {
                        InitialPage()
                    }
                    uiState.showNoDataPage -> {
                        NoDataPage()
                    }
                    uiState.showProgress -> {
                        ProgressPage()
                    }
                }
            }
        )
    }
}

@Composable
fun TopAppBar(onBackPress: () -> Unit, onSearchAction: (query: String) -> Unit){
    Column() {
        TopAppBar(
            elevation = 4.dp,
            title = {
                Text("Jetpack Compose")
            },
            backgroundColor =  MaterialTheme.colors.primarySurface,
            navigationIcon = {
                IconButton(onClick = onBackPress) {
                    Icon(Icons.Filled.ArrowBack, null)
                }
            }, actions = {
                IconButton(onClick = {/* Do Something*/ }) {
                    Icon(Icons.Filled.Share, null)
                }
                IconButton(onClick = {/* Do Something*/ }) {
                    Icon(Icons.Filled.Settings, null)
                }
            }
        )

        SearchOutlinedTextField(onSearchAction)

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchOutlinedTextField(onSearchAction: (query: String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("") },
        maxLines = 1,
        singleLine = true,
//        textStyle = TextStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_baseline_search_24),
                contentDescription = null
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            onSearchAction(text)
            focusManager.clearFocus()
        })
    )
}

@Composable
fun InitialPage() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()


    ){
        Text(
            text = stringResource(id = R.string.initial_page_description),
            style = MaterialTheme.typography.h6
        )
    }
}

@Composable
fun NoDataPage() {
    Box(
        contentAlignment = Alignment.Center,
    ){
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment =  Alignment.Companion.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.img_no_data),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                contentScale = ContentScale.FillWidth,
                contentDescription = null,
            )
            Text(
                text = stringResource(id = R.string.book_list_no_data_msg),
                style = MaterialTheme.typography.h6
            )            
        }
    }
}

@Composable
fun ProgressPage() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
    ){
        CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}

@Composable
fun BookListPage(
    pagingData: StateFlow<PagingData<ListItemViewType<BookItem>>>,
    lazyListState: LazyListState,
    onLoadStateChanged: (loadState: CombinedLoadStates, itemCount: Int) -> Unit,
    onBookClicked: (id: String) -> Unit
) {
    val lazyPagingItems = pagingData.collectAsLazyPagingItems()
    onLoadStateChanged(lazyPagingItems.loadState, lazyPagingItems.itemCount)

    LazyColumn(
        state = lazyListState
    ) {
//        itemsIndexed(
//            items = lazyPagingItems
//        ) { index, bookItem ->
//            if( bookItem != null ){
//                BookItemCell(bookItem)
//            }
//
//            if (index < lazyPagingItems.itemCount - 1)
//                Divider(color = Color.LightGray, thickness = 0.5.dp)
//        }

        items(lazyPagingItems) { dataItem ->
            when( dataItem ){
                is ListItemViewType.Data -> {
                    if( dataItem.data != null ){
                        BookItemCell(dataItem.data, onBookClicked)
                    }
                }
                is ListItemViewType.Separator -> {
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                }
                null -> {

                }
            }

//            if (index < lazyPagingItems.itemCount - 1)
//                Divider(color = Color.LightGray, thickness = 0.5.dp)
        }

        //마지막 행에 프로그레스바 표시
        lazyPagingItems.apply{
            when{
                loadState.append is LoadState.Loading -> {
                   item {
                       ProgressCell()
                   }
                }
            }
        }
    }
}

@Composable
fun ProgressCell() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ){
        CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}

@Composable
fun BookItemCell(bookItem: BookItem, onBookClicked: (id: String) -> Unit) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = bookItem.imageLinks?.thumbnail)
            .fallback(R.color.lightGray)
            .crossfade(true)
            .build()
    )

    var selectedBookItemId by remember{mutableStateOf( "")}

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .selectable(
                selected = bookItem.id == selectedBookItemId,
                onClick = {
                    selectedBookItemId = bookItem.id

                    onBookClicked(bookItem.id)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(45.dp),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column() {
            Text(
                text = bookItem.title!!,
                style = MaterialTheme.typography.h6,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            if( bookItem.saleability ){
                Row() {
                    Text(
                        text = stringResource(id = R.string.thousand_comma_price, bookItem.listPrice ?: 0),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = stringResource(id = R.string.thousand_comma_price_with_braket, bookItem.retailPrice ?: 0),
                        style = MaterialTheme.typography.caption,
                        textDecoration = TextDecoration.LineThrough
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "${bookItem.discountRatio}%",
                        style = MaterialTheme.typography.caption,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }
            }else{
                Text(
                    text = stringResource(id = R.string.saleability_not_for_sale),
                    style = MaterialTheme.typography.caption,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = bookItem.strAuthors,
                style = MaterialTheme.typography.caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = bookItem.publishedDate.toString(),
                style = MaterialTheme.typography.caption
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview(@PreviewParameter(SamplBookItemListProvider::class) bookItemList: BookItemList) {
////    val viewModel = hiltViewModel<ComposeBookListViewModel>()
//
//    AndroidTrainingTheme {
//        SearchOutlinedTextField({})
////        Screen(viewModel)
//    }
//}
//
//class SamplBookItemListProvider: PreviewParameterProvider<BookItemList> {
//    override val values = sequenceOf(
//        BookItemList(
//            meta = BookItemList.Meta(1, 40, 30),
//            items = getSampleBookItem()
//        )
//    )
//
//    fun getSampleBookItem(): ArrayList<BookItem> {
//        val listBookITem = ArrayList<BookItem>()
//        for(i in 1..40){
//            listBookITem.add(
//                BookItem(
//                    i.toLong(),
//                    "7LeZzQEACAAJ",
//                    "$i 프로페셔널 안드로이드(4판)(제이펍의 모바일 시리즈 37)",
//                    "",
//                    "※ 이 책은 PDF 형태로 제공하므로 화면이 작은 단말기(스마트폰)에서는 보기 불편할 수 있습니다. ※ 안드로이드 분야 1위 도서! 《Do it! 안드로이드 앱 프로그래밍》 개정 8판이 나왔다! 안드로이드 11 버전과 안드로이드 스튜디오 4.2 버전을 반영한 《Do it! 안드로이드 앱 프로그래밍》의 개정 8판이 나왔다. 이번 개정판에도 입문자를 위한 안드로이드 스튜디오 사용법을 담았고, 그동안 독자에게 받은 질문 등을 반영해 편의와 완성도를 높였다. 또한 소스 코드의 호환성을 검증하고 최근 경향에 맞게 예제를 직접 설계하고 다듬었다. 강사를 길러 내는 명강사로 알려진 안드로이드 전문가이자 프로그래머인 저자의 명쾌한 설명과 함께 모바일 앱 개발을 시작해 보자. 저자의 동영상 강의를 유튜브에서 무료로 시청할 수 있어서 전문 학원에 다니는 것처럼 배울 수 있다.",
//                    listOf("리토마이어1", "리토마이어2"),
//                    "publisher",
//                    "2019-11-12",
//                    "FOR_SALE",
//                    28000,
//                    25200,
//                    listOf("Technology", "Engineering"),
//                    "mainCategory",
//                    "9791188621583",
//                    "isbn13",
//                    imageLinks = BookItem.ImageLinks(
//                        thumbnail = "http://books.google.com/books/content?id=Y_s8EAAAQBAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api",
//                        cover = null
//                    ),
//                    bookDataSource = BookRepository.BookDataSource.GoogleBooks
//                )
//            )
//        }
//
//        return listBookITem
//    }
//}