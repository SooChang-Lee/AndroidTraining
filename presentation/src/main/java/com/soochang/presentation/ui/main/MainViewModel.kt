package com.soochang.presentation.ui.main

import androidx.lifecycle.viewModelScope
import com.soochang.domain.model.Result
import com.soochang.domain.model.main.MainMenu
import com.soochang.domain.repository.main.MainRepository
import com.soochang.presentation.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Flow를 사용하면서 유용하게 사용할 수 있는 state flow와 shared flow가 다른 점과 각각 어떤 상황에서 적합한지를 알기 위하여 두 개의 특성을 비교하려고 합니다.
 * Flow builder로 생성한 flow들은 기본적으로 Cold stream 입니다.[2] 하지만 StateFlow와 SharedFlow는 둘다 Hot stream입니다.
 * 이해를 돕기 위해 두 stream을 간단히 비교하면 아래와 같습니다.
 *
 * Cold stream
 * collect() (또는 이를 subscribe 할 때)를 호출할 때마다 flow block이 재실행 된다.
 * 즉 1~10까지 emit 하는 flow가 있다면 collect 할때마다 1~10을 전달 받는다. 여러곳에서 collect를 호출하면 각각의 collect에서 1~10을 전달받는다.
 * 출처: https://tourspace.tistory.com/434 [투덜이의 리얼 블로그:티스토리]
 *
 * Hot stream
 * collect (또는 이를 subscribe 할때)를 호출하더라도 flow block이 호출되지 않는다. collect() 시점 이후에 emit 된 데이터를 전달받는다.
 *
 * StateFlow
 * 기본적으로 사용하던 Flow 와 달리 기본값을 가지며, 마지막으로 emit 된 값 또한 상태로 가지고 있습니다. SharedFlow 를 확장한 클래스입니다.
 *
 * SharedFlow
 * 여러개의 collector에서 같은 값을 받을 수 있도록 구현된 Flow 입니다.
 */

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository
): BaseViewModel() {
    private val _mainMenu = MutableStateFlow<Result<MainMenu>?>(null)
    val mainMenu = _mainMenu.asStateFlow()

    init {
        viewModelScope.launch {
            _mainMenu.value = mainRepository.getMainMenu()
        }
    }
}