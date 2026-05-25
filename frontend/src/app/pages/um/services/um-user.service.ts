import { Injectable } from '@angular/core';
import { Observable, from, switchMap } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { PasswordCryptoService } from 'src/app/core/crypto/password-crypto.service';
import {
  AddUserRequest,
  AddUserResponse,
  DeleteUserRequest,
  DeleteUserResponse,
  GetUserRequest,
  GetUserResponse,
  GetsUsersRequest,
  GetsUsersResponse,
  UpdateUserRequest,
  UpdateUserResponse,
  UpdateProfileSelfRequest,
} from 'src/app/core/models/um.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({
  providedIn: 'root',
})
export class UmUserService {
  constructor(
    private readonly api: BusinessApiService,
    private readonly crypto: PasswordCryptoService
  ) {}

  gets(body: GetsUsersRequest): Observable<GetsUsersResponse> {
    return this.api.postEnvelope<GetsUsersResponse>(GlobalConstants.API_ENDPOINTS.um.user.gets, body);
  }

  get(body: GetUserRequest): Observable<GetUserResponse> {
    return this.api.postEnvelope<GetUserResponse>(GlobalConstants.API_ENDPOINTS.um.user.get, body);
  }

  /** Builds {@link AddUserRequest} with RSA-OAEP SHA-256 Base64 password per backend contract. */
  add(
    body: Omit<AddUserRequest, 'password'> & {
      passwordPlain: string;
      profileImageMimeType?: string;
      profileImageBase64?: string;
    }
  ): Observable<AddUserResponse> {
    return from(this.crypto.encryptRsaOaepSha256Base64(body.passwordPlain)).pipe(
      switchMap((password) => {
        const request: AddUserRequest = {
          username: body.username,
          firstName: body.firstName,
          lastName: body.lastName,
          email: body.email,
          mobileNumber: body.mobileNumber,
          password,
          roleIds: body.roleIds,
          status: body.status,
          ...(body.profileImageMimeType && body.profileImageBase64
            ? {
                profileImageMimeType: body.profileImageMimeType,
                profileImageBase64: body.profileImageBase64,
              }
            : {}),
        };
        return this.api.postEnvelope<AddUserResponse>(
          GlobalConstants.API_ENDPOINTS.um.user.add,
          request,
          'success-and-errors'
        );
      })
    );
  }

  update(body: UpdateUserRequest): Observable<UpdateUserResponse> {
    return this.api.postEnvelope<UpdateUserResponse>(
      GlobalConstants.API_ENDPOINTS.um.user.update,
      body,
      'success-and-errors'
    );
  }

  delete(body: DeleteUserRequest): Observable<DeleteUserResponse> {
    return this.api.postEnvelope<DeleteUserResponse>(
      GlobalConstants.API_ENDPOINTS.um.user.delete,
      body,
      'success-and-errors'
    );
  }

  updateSelfProfile(body: UpdateProfileSelfRequest): Observable<UpdateUserResponse> {
    return this.api.putEnvelope<UpdateUserResponse>(
      GlobalConstants.API_ENDPOINTS.um.user.updateProfile,
      body,
      'success-and-errors'
    );
  }
}
