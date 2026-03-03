#
# Be sure to run `pod lib lint KuiklyxBridgeNative.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'KuiklyxBridgeNative'
  s.version          = '1.3.0'
  s.summary          = 'A short description of KuiklyxBridgeNative.'

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/Kuikly-contrib/kuiklyx-bridge'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'anthropic' => '' }
  s.source           = { :git => 'https://github.com/Kuikly-contrib/kuiklyx-bridge.git', :tag => s.version.to_s }

  s.ios.deployment_target = '12.0'

  s.static_framework = true

  s.libraries    = "c++"
  s.source_files = 'KuiklyxBridgeNative/Classes/**/*'
  
  s.pod_target_xcconfig = {
    'OTHER_LDFLAGS' => '$(inherited)'
  }

  s.dependency 'TDFCommon'
  s.dependency 'KuiklyIOSRender'

end
