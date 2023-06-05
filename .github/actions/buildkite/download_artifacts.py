from typing import Optional
from dataclasses import dataclass

import globber
import requests
import os


BUILDKITE_API_ACCESS_TOKEN = os.environ["BUILDKITE_API_ACCESS_TOKEN"]
HEADERS = {'Authorization': f'Bearer {BUILDKITE_API_ACCESS_TOKEN}'}


@dataclass
class ListArtifactsRequestURLBuilder:
    org: str
    pipeline: str
    build_number: str
    per_page = 100

    def get_url(self) -> str:
        return (
            'https://api.buildkite.com/v2'
            f'/organizations/{self.org}'
            f'/pipelines/{self.pipeline}'
            f'/builds/{self.build_number}'
            '/artifacts'
            f'?per_page={self.per_page}'
        )


def find_all_matching_artifacts(artifacts: list, pattern: str) -> list:
    """
    Finds all artifacts matching the given pattern.
    :param artifacts: https://buildkite.com/docs/apis/rest-api/artifacts#list-artifacts-for-a-build
    :param pattern: glob path
    :return: list of artifacts
    """
    return [artifact for artifact in artifacts if globber.match(pattern, artifact["path"])]


def get_next_artifacts_url(artifacts_response) -> Optional[str]:
    """
    Gets the next artifacts list URL from an artifacts list response.
    https://buildkite.com/docs/apis/rest-api/artifacts#list-artifacts-for-a-build
    :param artifacts_response: Paginated list response (https://buildkite.com/docs/apis/rest-api#pagination)
    :return: The next artifacts url or None
    """
    if "next" in artifacts_response.links:
        return artifacts_response.links["next"]["url"]
    else:
        return None


def download_artifacts(artifacts: list) -> None:
    """
    Downloads artifacts to the current directory and maintains the folder structure.
    :param artifacts: https://buildkite.com/docs/apis/rest-api/artifacts#list-artifacts-for-a-build
    """
    for artifact in artifacts:
        # https://buildkite.com/docs/apis/rest-api/artifacts#download-an-artifact
        download_response = requests.get(
            artifact["download_url"],
            headers=HEADERS,
            allow_redirects=True
        )
        os.makedirs(artifact["dirname"], exist_ok=True)
        with open(artifact["path"], 'wb') as f:
            f.write(download_response.content)


def get_artifacts_to_download(pattern: str, request_url: str, artifacts: list = None) -> list:
    """
    Recursively gets all artifacts match the given pattern from a paginated artifacts list request.
    See https://buildkite.com/docs/apis/rest-api/artifacts#list-artifacts-for-a-build
    :param pattern: glob path
    :param request_url: artifacts list request URL
    :param artifacts: buildkite artifacts
    :return:
    """
    if artifacts is None:
        artifacts = []
    response = requests.get(
        request_url,
        headers=HEADERS
    )
    matching_artifacts = find_all_matching_artifacts(
        response.json(),
        pattern
    )
    next_url = get_next_artifacts_url(response)
    combined_artifacts = artifacts + matching_artifacts
    if next_url is None:
        return combined_artifacts
    else:
        return get_artifacts_to_download(pattern, next_url, combined_artifacts)


def run() -> None:
    org = os.environ["ORG"]
    pipeline = os.environ["PIPELINE"]
    build_number = os.environ["BUILD_NUMBER"]
    artifact_path = os.environ["ARTIFACT_PATH"]
    request = ListArtifactsRequestURLBuilder(org, pipeline, build_number)
    artifacts_to_download = get_artifacts_to_download(
        artifact_path,
        request.get_url()
    )
    download_artifacts(artifacts_to_download)


run()
