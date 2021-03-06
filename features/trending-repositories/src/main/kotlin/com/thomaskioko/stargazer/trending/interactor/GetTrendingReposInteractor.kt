package com.thomaskioko.stargazer.trending.interactor

import com.thomaskioko.stargazer.core.ViewStateResult
import com.thomaskioko.stargazer.core.injection.annotations.IoDispatcher
import com.thomaskioko.stargazer.core.interactor.Interactor
import com.thomaskioko.stargazer.repository.GithubRepository
import com.thomaskioko.stargazer.trending.model.RepoViewDataModel
import com.thomaskioko.stargazer.trending.model.ViewDataMapper.mapEntityListToRepoViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class GetTrendingReposInteractor @Inject constructor(
    private val repository: GithubRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : Interactor<Boolean, List<RepoViewDataModel>>() {

    override fun run(params: Boolean): Flow<ViewStateResult<List<RepoViewDataModel>>> =
        repository.getTrendingTrendingRepositories(params)
            .map { ViewStateResult.success(mapEntityListToRepoViewModel(it)) }
            .catch { emit(ViewStateResult.error(it.message.orEmpty())) }
            .flowOn(ioDispatcher)
}
