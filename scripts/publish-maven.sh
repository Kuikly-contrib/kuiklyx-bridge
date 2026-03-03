#!/usr/bin/env bash
#
# publish-maven
#
# 说明
#   1. 发布产物
#   2. 若未指定参数，则使用项目中的 publish.properties 配置
#
#

SHELL_DIR=$(
  cd "$(dirname "$0")" || exit 1
  pwd
)

PROJECT_DIR=$(realpath "${SHELL_DIR}/../")

GRADLEW="./gradlew"


# Kotlin 版本列表
KOTLIN_VERSION_LIST="2.0.21"
# 支持 ohos 的 kotlin 版本列表
OHOS_KOTLIN_VERSION_LIST="2.0.21-KBA-010"

# 默认 kuikly core 版本
DEFAULT_KUIKLY_CORE_VERSION="2.11.0"

# 映射关系：Kotlin版本=kuikly版本
KOTLIN_TO_KUIKLY_MAP=(
  "2.0.21=2.11.0"
  "2.0.21-KBA-010=2.11.0"
)

# publish module
PUBLISH_MODULE="shared_bridge"

# publish 任务
PUBLISH_TASK=publishAllPublicationsToMavenRepository
# publish cnb 任务
PUBLISH_TASK_CNB=publishAllPublicationsToCnbRepository

# 发布配置
BASE_VERSION=
PUB_ENABLE_SNAPSHOT=

function usage() {
  echo " Publish artifacts to a maven repo!"
  echo " Usage: publish-maven.sh [option] <value>"
  echo " Options:"
  echo "  -k, --kotlin        指定编译的 Kotlin 版本列表, 使用 , 分割, 默认: 1.6.21,1.7.20,1.9.22"
  echo "  -s, --snapshot      指定是否 SNAPSHOT，若未指定，则使用 default properties 配置"
  echo "  -l, --local-repo    指定是否是本地 repo, 默认 false"
  echo "  -c, --clean         指定是否需要 clean 后编译"
  echo "  -h, --help          查看帮助"
  echo " 示例:"
  echo " ./publish-maven.sh -l true"
  echo " ./publish-maven.sh -k 1.6.21"
  echo " ./publish-maven.sh -k 1.6.21,1.7.20"
}

function get_kuikly_core_version() {
  local kotlin_version="$1"
  for mapping in "${KOTLIN_TO_KUIKLY_MAP[@]}"; do
    key="${mapping%%=*}"
    value="${mapping#*=}"
    if [[ "$key" == "$kotlin_version" ]]; then
      echo "$value"
      return
    fi
  done
  echo "$DEFAULT_KUIKLY_CORE_VERSION"
}

function check() {
  cd "${PROJECT_DIR}" || return 1

  echo "> [INFO ] PUBLISH_MODULE: ${PUBLISH_MODULE}"

  # clean
  if [ "${NEED_CLEAN}" = 'true' ]; then
    echo "> [INFO ] start gradle clean!"
    if ! $GRADLEW clean; then
      return 1
    fi
  else
    echo "> [WARN ] skip gradle clean!"
  fi

  # 加载默认配置
  source "${PROJECT_DIR}/${PUBLISH_MODULE}/publish.properties"
  source "${PROJECT_DIR}/version.properties"

  BASE_VERSION=${version}
  # 优先外部设置, 其次读本地配置
  if [[ -z "${PUB_ENABLE_SNAPSHOT}" ]]; then
    PUB_ENABLE_SNAPSHOT=${snapshot}
    echo "> [INFO ] SNAPSHOT Not set, will use default value: ${snapshot}"
  else
    echo "> [INFO ] SNAPSHOT set: ${PUB_ENABLE_SNAPSHOT}"
  fi
}

function publishMaven() {
  local project=$1
  local task=$2
  echo "> [INFO ] upload project: $project"

  local snapshot
  if [[ -z "${PUB_ENABLE_SNAPSHOT}" ]]; then
    echo "> [INFO ] SNAPSHOT Not set, will use default properties"
  else
    snapshot="-PPUB_ENABLE_SNAPSHOT=${PUB_ENABLE_SNAPSHOT}"
  fi

  local localRepo
  if [[ -z "${PUB_IS_LOCAL_REPO}" ]]; then
    echo "> [INFO ] local repo Not set."
  else
    localRepo="-PPUB_IS_LOCAL_REPO=${PUB_IS_LOCAL_REPO}"
  fi

  local debug
  if [[ "${DEBUG}" == 'true' ]]; then
     debug="--debug"
  fi

  local build_version
  local final_versions

  # Kotlin version list
  echo "> [INFO ] Processing standard Kotlin versions..."
  IFS=',' read -r -a VERSIONS <<<"${KOTLIN_VERSION_LIST}"
  for kt_version in "${VERSIONS[@]}"; do
    local core_version=$(get_kuikly_core_version "$kt_version")
    echo "> [build] ${GRADLEW}:${project}:${task} -PkotlinVersion=${kt_version} -PkuiklyCoreVersion=${core_version} -PenableOHOS=false ${snapshot} ${localRepo}"
    if ! ${GRADLEW} :${project}:${task} -PkotlinVersion=${kt_version} -PkuiklyCoreVersion=${core_version} -PenableOHOS=false ${snapshot} ${localRepo} ${debug}; then
      echo ">[ERROR] build failed!"
      exit 1
    else
      if [[ "${PUB_ENABLE_SNAPSHOT}" == 'true' ]]; then
        build_version="${BASE_VERSION}-${kt_version}-SNAPSHOT"
      else
        build_version="${BASE_VERSION}-${kt_version}"
      fi
      if [[ -z "${final_versions}" ]]; then
        final_versions="${build_version}"
      else
        final_versions="${final_versions} | ${build_version}"
      fi
      echo "> build version: ${build_version}"
    fi
  done

  # Kotlin version list support ohos
  echo "> [INFO ] Processing OHOS Kotlin versions..."
  IFS=',' read -r -a OHOS_VERSIONS <<<"${OHOS_KOTLIN_VERSION_LIST}"
  for kt_version in "${OHOS_VERSIONS[@]}"; do
    local core_version=$(get_kuikly_core_version "$kt_version")
    echo "> [build] ${GRADLEW}:${project}:${task} -PkotlinVersion=${kt_version} -PkuiklyCoreVersion=${core_version} -PenableOHOS=true ${snapshot} ${localRepo}"
    if ! ${GRADLEW} :${project}:${task} -PkotlinVersion=${kt_version} -PkuiklyCoreVersion=${core_version} -PenableOHOS=true ${snapshot} ${localRepo} ${debug}; then
      echo ">[ERROR] build failed!"
      exit 1
    else
      if [[ "${PUB_ENABLE_SNAPSHOT}" == 'true' ]]; then
        build_version="${BASE_VERSION}-${kt_version}-SNAPSHOT"
      else
        build_version="${BASE_VERSION}-${kt_version}"
      fi
      if [[ -z "${final_versions}" ]]; then
        final_versions="${build_version}"
      else
        final_versions="${final_versions} | ${build_version}"
      fi
      echo "> build version: ${build_version}"
    fi
  done

  echo "> final versions: ${final_versions}"
  echo "::set-variable name=FINAL_VERSIONS::${final_versions}"
}

while [[ "$1" != "" ]]; do
  case $1 in
  -m | --module)
    shift
    PUBLISH_MODULE=$1
    ;;
  -k | --kotlin)
    shift
    KOTLIN_VERSION_LIST="${1#*=}"
    ;;
  -ok | --ohos-kotlin)
    shift
    OHOS_KOTLIN_VERSION_LIST="${1#*=}"
    ;;
  -s | --snapshot)
    shift
    PUB_ENABLE_SNAPSHOT=$1
    ;;
  -l | --local-repo)
    shift
    PUB_IS_LOCAL_REPO=$1
    ;;
  -c | --clean)
    shift
    NEED_CLEAN=$1
    ;;
  -cnb | --cnb)
    shift
    ENABLE_CNB=$1
    ;;
  -d | --debug)
    shift
    DEBUG=$1
    ;;
  -h | --help)
    usage
    exit
    ;;
  *)
    usage
    exit 1
    ;;
  esac
  shift
done

if ! check; then
  usage
  exit 1
fi

if ! publishMaven "${PUBLISH_MODULE}" "${PUBLISH_TASK}"; then
  exit 1
fi

if [[ "${ENABLE_CNB}" == 'true' ]]; then
  if ! publishMaven "${PUBLISH_MODULE}" "${PUBLISH_TASK_CNB}"; then
    exit 1
  fi
fi