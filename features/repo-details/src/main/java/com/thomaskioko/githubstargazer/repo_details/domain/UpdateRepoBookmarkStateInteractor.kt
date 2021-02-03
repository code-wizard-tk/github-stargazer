package com.thomaskioko.githubstargazer.repo_details.domain

import com.thomaskioko.githubstargazer.core.ViewState
import com.thomaskioko.githubstargazer.core.interactor.Interactor
import com.thomaskioko.githubstargazer.repo_details.domain.model.UpdateObject
import com.thomaskioko.githubstargazer.repo_details.model.RepoViewDataModel
import com.thomaskioko.githubstargazer.repo_details.model.ViewDataMapper.mapEntityToRepoViewModel
import com.thomaskioko.githubstargazer.repository.api.GithubRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal class UpdateRepoBookmarkStateInteractor @Inject constructor(
    private val repository: GithubRepository
) : Interactor<UpdateObject, RepoViewDataModel>() {

    override fun run(params: UpdateObject): Flow<ViewState<RepoViewDataModel>> = flow {

        emit(ViewState.loading())

        val isBookmarked = if (params.isBookmarked) 1 else 0

        repository.updateRepoBookMarkStatus(isBookmarked, params.repoId)

        val entity = repository.getRepoById(params.repoId)
        val repoViewDataModel = mapEntityToRepoViewModel(entity)

        emit(ViewState.success(repoViewDataModel))
    }.catch {
        emit(ViewState.error(it.message.orEmpty()))
    }
}
