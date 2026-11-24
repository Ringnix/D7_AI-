param(
    [string]$ABaseUrl = 'http://127.0.0.1:8081',
    [string]$BBaseUrl = 'http://127.0.0.1:8082',
    [string]$SeedOrderId = '910000000000000002'
)

$ErrorActionPreference = 'Stop'

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Uri,
        [object]$Body
    )

    $parameters = @{
        Uri = $Uri
        Method = $Method
        Headers = @{
            'Content-Type' = 'application/json'
            'X-Request-Id' = 'verify-' + [guid]::NewGuid().ToString('N')
        }
        TimeoutSec = 20
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $parameters.Body = $Body
    }

    try {
        $response = Invoke-WebRequest @parameters
        $text = if ($response.Content -is [byte[]]) {
            [Text.Encoding]::UTF8.GetString($response.Content)
        }
        else {
            [string]$response.Content
        }
        return [pscustomobject]@{
            Status = [int]$response.StatusCode
            Body = $text | ConvertFrom-Json
        }
    }
    catch {
        $response = $_.Exception.Response
        $status = if ($response) { [int]$response.StatusCode } else { 0 }
        $text = [string]$_.ErrorDetails.Message
        if (-not $text -and $response -and $response.PSObject.Methods['GetResponseStream']) {
            $reader = New-Object System.IO.StreamReader($response.GetResponseStream())
            try {
                $text = $reader.ReadToEnd()
            }
            finally {
                $reader.Dispose()
            }
        }
        return [pscustomobject]@{
            Status = $status
            Body = if ($text) { $text | ConvertFrom-Json } else { $null }
        }
    }
}

function Assert-Equal {
    param(
        $Actual,
        $Expected,
        [string]$Message
    )

    if ($Actual -ne $Expected) {
        throw "$Message. expected=$Expected actual=$Actual"
    }
}

$orderNo = 'VERIFY-' + (Get-Date -Format 'yyyyMMddHHmmssfff')
$createBody = @{
    orderNo = $orderNo
    customerId = 2001
    amount = 199.90
    status = 'CREATED'
} | ConvertTo-Json

$created = Invoke-Api -Method POST -Uri "$ABaseUrl/orders" -Body $createBody
Assert-Equal $created.Status 200 '创建订单失败'
$orderId = [long]$created.Body.data

$detail = Invoke-Api -Method GET -Uri "$ABaseUrl/orders/$orderId"
Assert-Equal $detail.Status 200 '详情查询失败'
Assert-Equal $detail.Body.data.orderNo $orderNo '详情订单号不一致'

$page = Invoke-Api -Method GET -Uri "$ABaseUrl/orders?current=1&pageSize=20"
Assert-Equal $page.Status 200 '分页查询失败'
if ($page.Body.data.datas.Count -gt 20) {
    throw '分页返回条数超过 pageSize'
}

$updateBody = @{
    orderNo = $orderNo
    customerId = 2001
    amount = 299.90
    status = 'PAID'
} | ConvertTo-Json
$updated = Invoke-Api -Method PUT -Uri "$ABaseUrl/orders/$orderId" -Body $updateBody
Assert-Equal $updated.Status 200 '修改订单失败'

$updatedDetail = Invoke-Api -Method GET -Uri "$ABaseUrl/orders/$orderId"
Assert-Equal $updatedDetail.Body.data.status 'PAID' '修改状态未持久化'

$duplicate = Invoke-Api -Method POST -Uri "$ABaseUrl/orders" -Body $createBody
Assert-Equal $duplicate.Status 409 '重复订单号未返回冲突'
Assert-Equal $duplicate.Body.errorCode '90000101' '冲突错误码不一致'

$invalidBody = @{
    orderNo = ''
    customerId = 0
    amount = -1
    status = 'CREATED'
} | ConvertTo-Json
$invalid = Invoke-Api -Method POST -Uri "$ABaseUrl/orders" -Body $invalidBody
Assert-Equal $invalid.Status 400 '非法参数未返回校验错误'

$deleted = Invoke-Api -Method DELETE -Uri "$ABaseUrl/orders/$orderId"
Assert-Equal $deleted.Status 200 '删除订单失败'

$missing = Invoke-Api -Method GET -Uri "$ABaseUrl/orders/$orderId"
Assert-Equal $missing.Status 404 '删除后查询未返回不存在'
Assert-Equal $missing.Body.errorCode '90000102' '不存在错误码不一致'

$hello = Invoke-Api -Method GET -Uri "$ABaseUrl/demo/hello?name=World" -Body $null
Assert-Equal $hello.Status 200 '/demo/hello 请求失败'
Assert-Equal $hello.Body.data.message 'Hello, World!' 'Hello 响应消息不一致'

$remoteDetail = Invoke-Api -Method GET -Uri "$BBaseUrl/consumer/orders/$SeedOrderId"
Assert-Equal $remoteDetail.Status 200 'B 服务 Feign 详情调用失败'

$remotePage = Invoke-Api -Method GET -Uri "$BBaseUrl/consumer/orders?current=1&pageSize=20"
Assert-Equal $remotePage.Status 200 'B 服务 Feign 分页调用失败'

[pscustomobject]@{
    create = 'passed'
    detail = 'passed'
    page = 'passed'
    update = 'passed'
    duplicateConflict = 'passed'
    validation = 'passed'
    delete = 'passed'
    deletedLookup = 'passed'
    hello = 'passed'
    feignDetail = 'passed'
    feignPage = 'passed'
} | Format-List
